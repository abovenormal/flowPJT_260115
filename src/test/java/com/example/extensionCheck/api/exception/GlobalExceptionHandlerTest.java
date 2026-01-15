package com.example.extensionCheck.api.exception;

import com.example.extensionCheck.api.Controller.ExtensionController;
import com.example.extensionCheck.api.Service.ExtensionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @Mock
    private ExtensionService extensionService;

    @InjectMocks
    private ExtensionController extensionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(extensionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("ExtensionException 처리")
    class HandleExtensionException {

        @Test
        @DisplayName("EMPTY_INPUT - BAD_REQUEST 반환")
        void handleEmptyInput_badRequest() throws Exception {
            when(extensionService.addExtension(any()))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.EMPTY_INPUT));

            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXT_001"))
                    .andExpect(jsonPath("$.message").value("확장자를 입력해주세요."));
        }

        @Test
        @DisplayName("TOO_LONG - BAD_REQUEST 반환")
        void handleTooLong_badRequest() throws Exception {
            when(extensionService.addExtension(any()))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.TOO_LONG));

            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "toolongextension"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXT_002"));
        }

        @Test
        @DisplayName("CONTAINS_DIGIT - BAD_REQUEST 반환")
        void handleContainsDigit_badRequest() throws Exception {
            when(extensionService.addExtension(any()))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.CONTAINS_DIGIT));

            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "pdf123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXT_003"));
        }

        @Test
        @DisplayName("CONTAINS_KOREAN - BAD_REQUEST 반환")
        void handleContainsKorean_badRequest() throws Exception {
            when(extensionService.addExtension(any()))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.CONTAINS_KOREAN));

            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "한글"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXT_004"));
        }

        @Test
        @DisplayName("ALREADY_EXISTS - CONFLICT 반환")
        void handleAlreadyExists_conflict() throws Exception {
            when(extensionService.addExtension(any()))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.ALREADY_EXISTS));

            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "pdf"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EXT_005"));
        }

        @Test
        @DisplayName("NOT_FOUND - NOT_FOUND 반환")
        void handleNotFound_notFound() throws Exception {
            doThrow(new ExtensionException(ExtensionErrorCode.NOT_FOUND))
                    .when(extensionService).deactivateExtension(any());

            mockMvc.perform(delete("/api/extensions/custom/notexist"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("EXT_006"));
        }

        @Test
        @DisplayName("MAX_LIMIT_EXCEEDED - UNPROCESSABLE_ENTITY 반환")
        void handleMaxLimitExceeded_unprocessableEntity() throws Exception {
            when(extensionService.addExtension(any()))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.MAX_LIMIT_EXCEEDED));

            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "pdf"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("EXT_007"));
        }

        @Test
        @DisplayName("FIXED_EXTENSION_CONFLICT - CONFLICT 반환")
        void handleFixedConflict_conflict() throws Exception {
            when(extensionService.addExtension(any()))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.FIXED_EXTENSION_CONFLICT));

            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "exe"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EXT_008"));
        }
    }

    @Nested
    @DisplayName("일반 예외 처리")
    class HandleGeneralException {

        @Test
        @DisplayName("IllegalArgumentException - BAD_REQUEST 반환")
        void handleIllegalArgument_badRequest() throws Exception {
            when(extensionService.addExtension(any()))
                    .thenThrow(new IllegalArgumentException("잘못된 입력입니다."));

            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "test"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("잘못된 입력입니다."));
        }

        @Test
        @DisplayName("RuntimeException - INTERNAL_SERVER_ERROR 반환")
        void handleRuntimeException_internalError() throws Exception {
            when(extensionService.addExtension(any()))
                    .thenThrow(new RuntimeException("예상치 못한 오류"));

            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "test"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        }
    }
}
