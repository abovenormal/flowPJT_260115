package com.example.extensionCheck.api.Controller;

import com.example.extensionCheck.api.Service.ExtensionService;
import com.example.extensionCheck.api.request.FixedBatchRequest;
import com.example.extensionCheck.api.response.ApiResponse;
import com.example.extensionCheck.api.response.ExtensionListResponse;
import com.example.extensionCheck.api.response.ExtensionResponse;
import com.example.extensionCheck.entity.Extensions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/extensions")
public class ExtensionController {

    private final ExtensionService extensionService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ExtensionResponse>> addExtension(
            @RequestParam String customExtension) {
        Extensions extension = extensionService.addExtension(customExtension);
        return ResponseEntity.ok(
                ApiResponse.ok(ExtensionResponse.from(extension), "확장자가 추가되었습니다.")
        );
    }

    @DeleteMapping("/custom/{extName}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomExtension(
            @PathVariable String extName) {
        extensionService.deactivateExtension(extName);
        return ResponseEntity.ok(ApiResponse.ok("확장자가 삭제되었습니다."));
    }

    @PostMapping("/fixed/{extName}")
    public ResponseEntity<ApiResponse<ExtensionResponse>> saveFixedExtension(
            @PathVariable String extName) {
        Extensions extension = extensionService.saveFixedExtension(extName);
        return ResponseEntity.ok(
                ApiResponse.ok(ExtensionResponse.from(extension), "고정 확장자가 저장되었습니다.")
        );
    }

    @DeleteMapping("/fixed/{extName}")
    public ResponseEntity<ApiResponse<Void>> deleteFixedExtension(
            @PathVariable String extName) {
        extensionService.deleteFixedExtension(extName);
        return ResponseEntity.ok(ApiResponse.ok("고정 확장자가 해제되었습니다."));
    }

    @PatchMapping("/fixed/batch")
    public ResponseEntity<ApiResponse<Void>> batchUpdateFixed(
            @RequestBody FixedBatchRequest request) {
        extensionService.batchUpdateFixed(request.getChecked(), request.getUnchecked());
        return ResponseEntity.ok(ApiResponse.ok("배치 업데이트가 완료되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ExtensionListResponse>> getList() {
        ExtensionListResponse response = extensionService.getActiveExtensions();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
