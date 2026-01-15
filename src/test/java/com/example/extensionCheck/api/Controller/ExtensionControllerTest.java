package com.example.extensionCheck.api.Controller;

import com.example.extensionCheck.api.Service.ExtensionService;
import com.example.extensionCheck.api.exception.ExtensionErrorCode;
import com.example.extensionCheck.api.exception.ExtensionException;
import com.example.extensionCheck.api.exception.GlobalExceptionHandler;
import com.example.extensionCheck.api.response.ExtensionListResponse;
import com.example.extensionCheck.entity.ExtensionType;
import com.example.extensionCheck.entity.Extensions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExtensionController 테스트")
class ExtensionControllerTest {

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
    @DisplayName("POST /api/extensions/add")
    class AddExtension {

        @Test
        @DisplayName("확장자 추가 성공")
        void addExtension_success() throws Exception {
            // given
            Extensions ext = Extensions.builder()
                    .name("pdf").isActive(true).type(ExtensionType.CUSTOM).build();
            when(extensionService.addExtension("pdf")).thenReturn(ext);

            // when & then
            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "pdf"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("pdf"));
        }

        @Test
        @DisplayName("빈 입력 시 BAD_REQUEST 반환")
        void addExtension_emptyInput_badRequest() throws Exception {
            // given
            when(extensionService.addExtension(""))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.EMPTY_INPUT));

            // when & then
            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXT_001"));
        }

        @Test
        @DisplayName("숫자 포함 시 BAD_REQUEST 반환")
        void addExtension_containsDigit_badRequest() throws Exception {
            // given
            when(extensionService.addExtension("pdf123"))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.CONTAINS_DIGIT));

            // when & then
            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "pdf123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXT_003"));
        }

        @Test
        @DisplayName("중복 확장자 시 CONFLICT 반환")
        void addExtension_alreadyExists_conflict() throws Exception {
            // given
            when(extensionService.addExtension("pdf"))
                    .thenThrow(new ExtensionException(ExtensionErrorCode.ALREADY_EXISTS));

            // when & then
            mockMvc.perform(post("/api/extensions/add")
                            .param("customExtension", "pdf"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EXT_005"));
        }
    }

    @Nested
    @DisplayName("GET /api/extensions")
    class GetList {

        @Test
        @DisplayName("목록 조회 성공")
        void getList_success() throws Exception {
            // given
            ExtensionListResponse response = ExtensionListResponse.full(
                    List.of("exe", "bat"), List.of("pdf", "doc"));
            when(extensionService.getActiveExtensions()).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/extensions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fixed").isArray())
                    .andExpect(jsonPath("$.data.custom").isArray())
                    .andExpect(jsonPath("$.data.count").value(2));
        }
    }

    @Nested
    @DisplayName("DELETE /api/extensions/custom/{extName}")
    class DeleteCustomExtension {

        @Test
        @DisplayName("커스텀 확장자 삭제 성공")
        void deleteCustomExtension_success() throws Exception {
            // given
            doNothing().when(extensionService).deactivateExtension("pdf");

            // when & then
            mockMvc.perform(delete("/api/extensions/custom/pdf"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("존재하지 않는 확장자 삭제 시 NOT_FOUND 반환")
        void deleteCustomExtension_notFound() throws Exception {
            // given
            doThrow(new ExtensionException(ExtensionErrorCode.NOT_FOUND))
                    .when(extensionService).deactivateExtension("notexist");

            // when & then
            mockMvc.perform(delete("/api/extensions/custom/notexist"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("EXT_006"));
        }
    }

    @Nested
    @DisplayName("POST /api/extensions/fixed/{extName}")
    class SaveFixedExtension {

        @Test
        @DisplayName("고정 확장자 저장 성공")
        void saveFixedExtension_success() throws Exception {
            // given
            Extensions ext = Extensions.builder()
                    .name("exe").isActive(true).type(ExtensionType.FIXED).build();
            when(extensionService.saveFixedExtension("exe")).thenReturn(ext);

            // when & then
            mockMvc.perform(post("/api/extensions/fixed/exe"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("exe"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/extensions/fixed/{extName}")
    class DeleteFixedExtension {

        @Test
        @DisplayName("고정 확장자 삭제 성공")
        void deleteFixedExtension_success() throws Exception {
            // given
            doNothing().when(extensionService).deleteFixedExtension("exe");

            // when & then
            mockMvc.perform(delete("/api/extensions/fixed/exe"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /api/extensions/fixed/batch")
    class BatchUpdateFixed {

        @Test
        @DisplayName("배치 업데이트 성공")
        void batchUpdateFixed_success() throws Exception {
            // given
            String requestBody = "{\"checked\":[\"exe\",\"bat\"],\"unchecked\":[\"cmd\"]}";

            // when & then
            mockMvc.perform(patch("/api/extensions/fixed/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(extensionService).batchUpdateFixed(List.of("exe", "bat"), List.of("cmd"));
        }

        @Test
        @DisplayName("빈 요청 본문으로 배치 업데이트")
        void batchUpdateFixed_emptyBody_success() throws Exception {
            // given
            String requestBody = "{\"checked\":[],\"unchecked\":[]}";

            // when & then
            mockMvc.perform(patch("/api/extensions/fixed/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
