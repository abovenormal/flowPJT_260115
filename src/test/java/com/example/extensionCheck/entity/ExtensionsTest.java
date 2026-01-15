package com.example.extensionCheck.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Extensions Entity 테스트")
class ExtensionsTest {

    @Nested
    @DisplayName("activate 메서드")
    class Activate {

        @Test
        @DisplayName("isActive를 true로 변경한다")
        void activate_setsActiveTrue() {
            // given
            Extensions ext = Extensions.builder()
                    .name("pdf")
                    .isActive(false)
                    .type(ExtensionType.CUSTOM)
                    .build();

            // when
            ext.activate();

            // then
            assertThat(ext.isActive()).isTrue();
        }

        @Test
        @DisplayName("이미 활성화된 상태에서도 정상 동작한다")
        void activate_alreadyActive_remainsTrue() {
            // given
            Extensions ext = Extensions.builder()
                    .name("pdf")
                    .isActive(true)
                    .type(ExtensionType.CUSTOM)
                    .build();

            // when
            ext.activate();

            // then
            assertThat(ext.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("deactivate 메서드")
    class Deactivate {

        @Test
        @DisplayName("isActive를 false로 변경한다")
        void deactivate_setsActiveFalse() {
            // given
            Extensions ext = Extensions.builder()
                    .name("pdf")
                    .isActive(true)
                    .type(ExtensionType.CUSTOM)
                    .build();

            // when
            ext.deactivate();

            // then
            assertThat(ext.isActive()).isFalse();
        }

        @Test
        @DisplayName("이미 비활성화된 상태에서도 정상 동작한다")
        void deactivate_alreadyInactive_remainsFalse() {
            // given
            Extensions ext = Extensions.builder()
                    .name("pdf")
                    .isActive(false)
                    .type(ExtensionType.CUSTOM)
                    .build();

            // when
            ext.deactivate();

            // then
            assertThat(ext.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder 테스트")
    class BuilderTest {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void builder_setsAllFields() {
            // when
            Extensions ext = Extensions.builder()
                    .name("test")
                    .isActive(true)
                    .type(ExtensionType.FIXED)
                    .build();

            // then
            assertThat(ext.getName()).isEqualTo("test");
            assertThat(ext.isActive()).isTrue();
            assertThat(ext.getType()).isEqualTo(ExtensionType.FIXED);
        }

        @Test
        @DisplayName("CUSTOM 타입 확장자 생성")
        void builder_customType() {
            // when
            Extensions ext = Extensions.builder()
                    .name("pdf")
                    .isActive(true)
                    .type(ExtensionType.CUSTOM)
                    .build();

            // then
            assertThat(ext.getType()).isEqualTo(ExtensionType.CUSTOM);
        }

        @Test
        @DisplayName("FIXED 타입 확장자 생성")
        void builder_fixedType() {
            // when
            Extensions ext = Extensions.builder()
                    .name("exe")
                    .isActive(true)
                    .type(ExtensionType.FIXED)
                    .build();

            // then
            assertThat(ext.getType()).isEqualTo(ExtensionType.FIXED);
        }
    }
}
