package com.example.extensionCheck.api.Service;

import com.example.extensionCheck.api.exception.ExtensionErrorCode;
import com.example.extensionCheck.api.exception.ExtensionException;
import com.example.extensionCheck.api.response.ExtensionListResponse;
import com.example.extensionCheck.api.validator.ExtensionValidator;
import com.example.extensionCheck.entity.ExtensionType;
import com.example.extensionCheck.entity.Extensions;
import com.example.extensionCheck.repository.ExtensionsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtensionService {

    private final ExtensionsRepository extRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExtensionValidator validator;

    private static final int MAX_CUSTOM_EXTENSIONS = 200;
    private static final String WEBSOCKET_TOPIC = "/topic/extensions";

    /**
     * WebSocket으로 변경사항 브로드캐스트 (전체 데이터)
     */
    private void broadcastUpdate() {
        Map<String, Object> data = getActiveExtensionsMap();
        data.put("type", "full");
        messagingTemplate.convertAndSend(WEBSOCKET_TOPIC, (Object) data);
        log.debug("Broadcast full update");
    }

    /**
     * WebSocket으로 Delta 변경사항만 브로드캐스트
     */
    private void broadcastDeltaUpdate(List<String> fixedAdded, List<String> fixedRemoved) {
        Map<String, Object> delta = new HashMap<>();
        delta.put("type", "delta");
        delta.put("fixedAdded", fixedAdded != null ? fixedAdded : List.of());
        delta.put("fixedRemoved", fixedRemoved != null ? fixedRemoved : List.of());
        messagingTemplate.convertAndSend(WEBSOCKET_TOPIC, (Object) delta);
        log.debug("Broadcast delta update: added={}, removed={}", fixedAdded, fixedRemoved);
    }

    /**
     * 커스텀 확장자 추가
     */
    @Transactional
    public Extensions addExtension(String customExtension) {
        log.debug("Adding custom extension: {}", customExtension);

        validator.validate(customExtension);

        String lowerExt = customExtension.toLowerCase();

        long customCount = extRepository.countByTypeAndIsActiveTrue(ExtensionType.CUSTOM);
        if (customCount >= MAX_CUSTOM_EXTENSIONS) {
            throw new ExtensionException(ExtensionErrorCode.MAX_LIMIT_EXCEEDED);
        }

        List<String> fixedExtNames = extractNames(
                extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.FIXED)
        );

        if (fixedExtNames.contains(lowerExt)) {
            throw new ExtensionException(ExtensionErrorCode.FIXED_EXTENSION_CONFLICT);
        }

        var existingExt = extRepository.findByName(lowerExt);

        if (existingExt.isPresent()) {
            Extensions ext = existingExt.get();

            if (ext.isActive()) {
                throw new ExtensionException(ExtensionErrorCode.ALREADY_EXISTS);
            }

            ext.activate();
            broadcastUpdate();
            log.info("Reactivated extension: {}", lowerExt);
            return ext;
        }

        Extensions extensionsNew = Extensions.builder()
                .name(lowerExt)
                .isActive(true)
                .type(ExtensionType.CUSTOM)
                .build();

        Extensions saved = extRepository.save(extensionsNew);
        broadcastUpdate();
        log.info("Created new extension: {}", lowerExt);
        return saved;
    }

    /**
     * 고정 확장자 저장 (체크박스 체크 시)
     */
    @Transactional
    public Extensions saveFixedExtension(String name) {
        String lowerName = name.toLowerCase();
        log.debug("Saving fixed extension: {}", lowerName);

        Extensions result = extRepository.findByNameAndType(lowerName, ExtensionType.FIXED)
                .map(ext -> {
                    ext.activate();
                    return ext;
                })
                .orElseGet(() -> {
                    Extensions newExt = Extensions.builder()
                            .name(lowerName)
                            .isActive(true)
                            .type(ExtensionType.FIXED)
                            .build();
                    return extRepository.save(newExt);
                });

        broadcastUpdate();
        return result;
    }

    /**
     * 고정 확장자 삭제 (체크박스 해제 시)
     */
    @Transactional
    public void deleteFixedExtension(String name) {
        String lowerName = name.toLowerCase();
        log.debug("Deleting fixed extension: {}", lowerName);

        extRepository.findByNameAndType(lowerName, ExtensionType.FIXED)
                .ifPresent(Extensions::deactivate);

        broadcastUpdate();
    }

    /**
     * 활성화된 확장자 목록 조회 (DTO 반환)
     */
    public ExtensionListResponse getActiveExtensions() {
        List<String> fixedNames = extractNames(
                extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.FIXED)
        );
        List<String> customNames = extractNames(
                extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.CUSTOM)
        );

        return ExtensionListResponse.full(fixedNames, customNames);
    }

    /**
     * 활성화된 확장자 목록 조회 (Map 반환 - WebSocket용)
     */
    public Map<String, Object> getActiveExtensionsMap() {
        List<String> fixedNames = extractNames(
                extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.FIXED)
        );
        List<String> customNames = extractNames(
                extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.CUSTOM)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("fixed", fixedNames);
        result.put("custom", customNames);
        result.put("count", customNames.size());
        return result;
    }

    /**
     * 커스텀 확장자 비활성화
     */
    @Transactional
    public void deactivateExtension(String name) {
        log.debug("Deactivating extension: {}", name);

        Extensions extension = extRepository.findByName(name)
                .orElseThrow(() -> new ExtensionException(ExtensionErrorCode.NOT_FOUND));

        extension.deactivate();
        broadcastUpdate();
        log.info("Deactivated extension: {}", name);
    }

    /**
     * 고정 확장자 배치 업데이트 (여러 체크박스 한 번에 처리)
     */
    @Transactional
    public void batchUpdateFixed(List<String> checked, List<String> unchecked) {
        log.debug("Batch update: checked={}, unchecked={}", checked, unchecked);

        if (checked != null) {
            for (String name : checked) {
                saveFixedExtensionInternal(name);
            }
        }

        if (unchecked != null) {
            for (String name : unchecked) {
                deleteFixedExtensionInternal(name);
            }
        }

        broadcastDeltaUpdate(checked, unchecked);
    }

    /**
     * 고정 확장자 저장 (내부용, 브로드캐스트 없음)
     */
    private void saveFixedExtensionInternal(String name) {
        String lowerName = name.toLowerCase();

        extRepository.findByNameAndType(lowerName, ExtensionType.FIXED)
                .ifPresentOrElse(
                        Extensions::activate,
                        () -> {
                            Extensions newExt = Extensions.builder()
                                    .name(lowerName)
                                    .isActive(true)
                                    .type(ExtensionType.FIXED)
                                    .build();
                            extRepository.save(newExt);
                        }
                );
    }

    /**
     * 고정 확장자 삭제 (내부용, 브로드캐스트 없음)
     */
    private void deleteFixedExtensionInternal(String name) {
        String lowerName = name.toLowerCase();

        extRepository.findByNameAndType(lowerName, ExtensionType.FIXED)
                .ifPresent(Extensions::deactivate);
    }

    /**
     * Extensions 리스트에서 이름만 추출
     */
    private List<String> extractNames(List<Extensions> extensions) {
        return extensions.stream()
                .map(Extensions::getName)
                .collect(Collectors.toList());
    }
}
