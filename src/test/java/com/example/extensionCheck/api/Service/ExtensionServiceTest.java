package com.example.extensionCheck.api.Service;

import com.example.extensionCheck.api.exception.ExtensionErrorCode;
import com.example.extensionCheck.api.exception.ExtensionException;
import com.example.extensionCheck.api.response.ExtensionListResponse;
import com.example.extensionCheck.api.validator.ExtensionValidator;
import com.example.extensionCheck.entity.ExtensionType;
import com.example.extensionCheck.entity.Extensions;
import com.example.extensionCheck.repository.ExtensionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExtensionService 테스트")
class ExtensionServiceTest {

    @Mock
    private ExtensionsRepository extRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Spy
    private ExtensionValidator validator = new ExtensionValidator();

    @InjectMocks
    private ExtensionService extensionService;

    @Nested
    @DisplayName("addExtension 메서드")
    class AddExtension {

        @Test
        @DisplayName("새로운 커스텀 확장자를 성공적으로 추가한다")
        void addExtension_success() {
            // given
            String extensionName = "pdf";
            Extensions savedExtension = Extensions.builder()
                    .name(extensionName)
                    .isActive(true)
                    .type(ExtensionType.CUSTOM)
                    .build();

            when(extRepository.countByTypeAndIsActiveTrue(ExtensionType.CUSTOM)).thenReturn(0L);
            when(extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.FIXED)).thenReturn(List.of());
            when(extRepository.findByName(extensionName)).thenReturn(Optional.empty());
            when(extRepository.save(any(Extensions.class))).thenReturn(savedExtension);

            // when
            Extensions result = extensionService.addExtension(extensionName);

            // then
            assertThat(result.getName()).isEqualTo(extensionName);
            assertThat(result.getType()).isEqualTo(ExtensionType.CUSTOM);
            assertThat(result.isActive()).isTrue();
            verify(extRepository).save(any(Extensions.class));
        }

        @Test
        @DisplayName("200개 초과 시 MAX_LIMIT_EXCEEDED 예외를 던진다")
        void addExtension_maxLimitExceeded_throwsException() {
            // given
            when(extRepository.countByTypeAndIsActiveTrue(ExtensionType.CUSTOM)).thenReturn(200L);

            // when & then
            assertThatThrownBy(() -> extensionService.addExtension("pdf"))
                    .isInstanceOf(ExtensionException.class)
                    .extracting(e -> ((ExtensionException) e).getErrorCode())
                    .isEqualTo(ExtensionErrorCode.MAX_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("고정 확장자와 동일한 이름이면 FIXED_EXTENSION_CONFLICT 예외를 던진다")
        void addExtension_fixedConflict_throwsException() {
            // given
            String extensionName = "exe";
            Extensions fixedExt = Extensions.builder()
                    .name(extensionName)
                    .isActive(true)
                    .type(ExtensionType.FIXED)
                    .build();

            when(extRepository.countByTypeAndIsActiveTrue(ExtensionType.CUSTOM)).thenReturn(0L);
            when(extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.FIXED)).thenReturn(List.of(fixedExt));

            // when & then
            assertThatThrownBy(() -> extensionService.addExtension(extensionName))
                    .isInstanceOf(ExtensionException.class)
                    .extracting(e -> ((ExtensionException) e).getErrorCode())
                    .isEqualTo(ExtensionErrorCode.FIXED_EXTENSION_CONFLICT);
        }

        @Test
        @DisplayName("이미 활성화된 확장자면 ALREADY_EXISTS 예외를 던진다")
        void addExtension_alreadyExists_throwsException() {
            // given
            String extensionName = "pdf";
            Extensions existingExt = Extensions.builder()
                    .name(extensionName)
                    .isActive(true)
                    .type(ExtensionType.CUSTOM)
                    .build();

            when(extRepository.countByTypeAndIsActiveTrue(ExtensionType.CUSTOM)).thenReturn(0L);
            when(extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.FIXED)).thenReturn(List.of());
            when(extRepository.findByName(extensionName)).thenReturn(Optional.of(existingExt));

            // when & then
            assertThatThrownBy(() -> extensionService.addExtension(extensionName))
                    .isInstanceOf(ExtensionException.class)
                    .extracting(e -> ((ExtensionException) e).getErrorCode())
                    .isEqualTo(ExtensionErrorCode.ALREADY_EXISTS);
        }

        @Test
        @DisplayName("비활성화된 확장자가 있으면 재활성화한다")
        void addExtension_reactivateDeactivated() {
            // given
            String extensionName = "pdf";
            Extensions deactivatedExt = Extensions.builder()
                    .name(extensionName)
                    .isActive(false)
                    .type(ExtensionType.CUSTOM)
                    .build();

            when(extRepository.countByTypeAndIsActiveTrue(ExtensionType.CUSTOM)).thenReturn(0L);
            when(extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.FIXED)).thenReturn(List.of());
            when(extRepository.findByName(extensionName)).thenReturn(Optional.of(deactivatedExt));

            // when
            Extensions result = extensionService.addExtension(extensionName);

            // then
            assertThat(result.isActive()).isTrue();
            verify(extRepository, never()).save(any(Extensions.class));
        }
    }

    @Nested
    @DisplayName("deactivateExtension 메서드")
    class DeactivateExtension {

        @Test
        @DisplayName("존재하는 확장자를 비활성화한다")
        void deactivateExtension_success() {
            // given
            String extensionName = "pdf";
            Extensions extension = Extensions.builder()
                    .name(extensionName)
                    .isActive(true)
                    .type(ExtensionType.CUSTOM)
                    .build();

            when(extRepository.findByName(extensionName)).thenReturn(Optional.of(extension));

            // when
            extensionService.deactivateExtension(extensionName);

            // then
            assertThat(extension.isActive()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 확장자면 NOT_FOUND 예외를 던진다")
        void deactivateExtension_notFound_throwsException() {
            // given
            when(extRepository.findByName(anyString())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> extensionService.deactivateExtension("notexist"))
                    .isInstanceOf(ExtensionException.class)
                    .extracting(e -> ((ExtensionException) e).getErrorCode())
                    .isEqualTo(ExtensionErrorCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getActiveExtensions 메서드")
    class GetActiveExtensions {

        @Test
        @DisplayName("활성화된 확장자 목록을 반환한다")
        void getActiveExtensions_success() {
            // given
            Extensions fixedExt = Extensions.builder().name("exe").type(ExtensionType.FIXED).isActive(true).build();
            Extensions customExt = Extensions.builder().name("pdf").type(ExtensionType.CUSTOM).isActive(true).build();

            when(extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.FIXED)).thenReturn(List.of(fixedExt));
            when(extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.CUSTOM)).thenReturn(List.of(customExt));

            // when
            ExtensionListResponse result = extensionService.getActiveExtensions();

            // then
            assertThat(result.getFixed()).containsExactly("exe");
            assertThat(result.getCustom()).containsExactly("pdf");
            assertThat(result.getCount()).isEqualTo(1);
            assertThat(result.getType()).isEqualTo("full");
        }
    }

    @Nested
    @DisplayName("saveFixedExtension 메서드")
    class SaveFixedExtension {

        @Test
        @DisplayName("새로운 고정 확장자를 저장한다")
        void saveFixedExtension_new_success() {
            // given
            String extName = "exe";
            Extensions newExt = Extensions.builder()
                    .name(extName).isActive(true).type(ExtensionType.FIXED).build();

            when(extRepository.findByNameAndType(extName, ExtensionType.FIXED))
                    .thenReturn(Optional.empty());
            when(extRepository.save(any(Extensions.class))).thenReturn(newExt);

            // when
            Extensions result = extensionService.saveFixedExtension(extName);

            // then
            assertThat(result.isActive()).isTrue();
            verify(extRepository).save(any(Extensions.class));
        }

        @Test
        @DisplayName("비활성화된 고정 확장자를 재활성화한다")
        void saveFixedExtension_reactivate_success() {
            // given
            String extName = "exe";
            Extensions deactivated = Extensions.builder()
                    .name(extName).isActive(false).type(ExtensionType.FIXED).build();

            when(extRepository.findByNameAndType(extName, ExtensionType.FIXED))
                    .thenReturn(Optional.of(deactivated));

            // when
            Extensions result = extensionService.saveFixedExtension(extName);

            // then
            assertThat(result.isActive()).isTrue();
            verify(extRepository, never()).save(any(Extensions.class));
        }
    }

    @Nested
    @DisplayName("deleteFixedExtension 메서드")
    class DeleteFixedExtension {

        @Test
        @DisplayName("고정 확장자를 비활성화한다")
        void deleteFixedExtension_success() {
            // given
            String extName = "exe";
            Extensions extension = Extensions.builder()
                    .name(extName).isActive(true).type(ExtensionType.FIXED).build();

            when(extRepository.findByNameAndType(extName, ExtensionType.FIXED))
                    .thenReturn(Optional.of(extension));

            // when
            extensionService.deleteFixedExtension(extName);

            // then
            assertThat(extension.isActive()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 고정 확장자는 무시한다")
        void deleteFixedExtension_notFound_ignored() {
            // given
            when(extRepository.findByNameAndType(anyString(), eq(ExtensionType.FIXED)))
                    .thenReturn(Optional.empty());

            // when & then (no exception)
            extensionService.deleteFixedExtension("notexist");
            verify(messagingTemplate).convertAndSend(eq("/topic/extensions"), any(Object.class));
        }
    }

    @Nested
    @DisplayName("batchUpdateFixed 메서드")
    class BatchUpdateFixed {

        @Test
        @DisplayName("여러 고정 확장자를 한 번에 업데이트한다")
        void batchUpdateFixed_success() {
            // given
            List<String> checked = List.of("exe", "bat");
            List<String> unchecked = List.of("cmd");

            // when
            extensionService.batchUpdateFixed(checked, unchecked);

            // then
            verify(extRepository, times(3)).findByNameAndType(anyString(), eq(ExtensionType.FIXED));
            verify(messagingTemplate).convertAndSend(eq("/topic/extensions"), any(Object.class));
        }

        @Test
        @DisplayName("빈 리스트로 호출해도 예외가 발생하지 않는다")
        void batchUpdateFixed_emptyLists_noException() {
            // when & then (no exception)
            extensionService.batchUpdateFixed(List.of(), List.of());
            verify(messagingTemplate).convertAndSend(eq("/topic/extensions"), any(Object.class));
        }

        @Test
        @DisplayName("null 리스트로 호출해도 예외가 발생하지 않는다")
        void batchUpdateFixed_nullLists_noException() {
            // when & then (no exception)
            extensionService.batchUpdateFixed(null, null);
            verify(messagingTemplate).convertAndSend(eq("/topic/extensions"), any(Object.class));
        }
    }

    @Nested
    @DisplayName("getActiveExtensionsMap 메서드")
    class GetActiveExtensionsMap {

        @Test
        @DisplayName("WebSocket용 Map을 반환한다")
        void getActiveExtensionsMap_success() {
            // given
            Extensions fixedExt = Extensions.builder().name("exe").type(ExtensionType.FIXED).isActive(true).build();
            Extensions customExt = Extensions.builder().name("pdf").type(ExtensionType.CUSTOM).isActive(true).build();

            when(extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.FIXED)).thenReturn(List.of(fixedExt));
            when(extRepository.findAllByTypeAndIsActiveTrue(ExtensionType.CUSTOM)).thenReturn(List.of(customExt));

            // when
            var result = extensionService.getActiveExtensionsMap();

            // then
            assertThat(result).containsKey("fixed");
            assertThat(result).containsKey("custom");
            assertThat(result).containsKey("count");
            assertThat((List<String>) result.get("fixed")).containsExactly("exe");
            assertThat((List<String>) result.get("custom")).containsExactly("pdf");
            assertThat(result.get("count")).isEqualTo(1);
        }
    }
}
