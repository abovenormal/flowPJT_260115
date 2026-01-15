package com.example.extensionCheck.api.validator;

import com.example.extensionCheck.api.exception.ExtensionErrorCode;
import com.example.extensionCheck.api.exception.ExtensionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("ExtensionValidator 테스트")
class ExtensionValidatorTest {

    private ExtensionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ExtensionValidator();
    }

    @Test
    @DisplayName("유효한 확장자는 예외 없이 통과한다")
    void validate_validExtension_noException() {
        assertDoesNotThrow(() -> validator.validate("pdf"));
        assertDoesNotThrow(() -> validator.validate("docx"));
        assertDoesNotThrow(() -> validator.validate("exe"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("빈 입력은 EMPTY_INPUT 예외를 던진다")
    void validate_emptyInput_throwsException(String input) {
        assertThatThrownBy(() -> validator.validate(input))
                .isInstanceOf(ExtensionException.class)
                .extracting(e -> ((ExtensionException) e).getErrorCode())
                .isEqualTo(ExtensionErrorCode.EMPTY_INPUT);
    }

    @Test
    @DisplayName("20자 초과 확장자는 TOO_LONG 예외를 던진다")
    void validate_tooLong_throwsException() {
        String longExtension = "a".repeat(21);

        assertThatThrownBy(() -> validator.validate(longExtension))
                .isInstanceOf(ExtensionException.class)
                .extracting(e -> ((ExtensionException) e).getErrorCode())
                .isEqualTo(ExtensionErrorCode.TOO_LONG);
    }

    @ParameterizedTest
    @ValueSource(strings = {"pdf1", "1exe", "doc2x", "123"})
    @DisplayName("숫자가 포함된 확장자는 CONTAINS_DIGIT 예외를 던진다")
    void validate_containsDigit_throwsException(String input) {
        assertThatThrownBy(() -> validator.validate(input))
                .isInstanceOf(ExtensionException.class)
                .extracting(e -> ((ExtensionException) e).getErrorCode())
                .isEqualTo(ExtensionErrorCode.CONTAINS_DIGIT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"한글", "pdf한글", "ㄱㄴㄷ", "ㅏㅓㅗ"})
    @DisplayName("한글이 포함된 확장자는 CONTAINS_KOREAN 예외를 던진다")
    void validate_containsKorean_throwsException(String input) {
        assertThatThrownBy(() -> validator.validate(input))
                .isInstanceOf(ExtensionException.class)
                .extracting(e -> ((ExtensionException) e).getErrorCode())
                .isEqualTo(ExtensionErrorCode.CONTAINS_KOREAN);
    }

    @Test
    @DisplayName("20자 이하 확장자는 예외 없이 통과한다")
    void validate_exactly20Chars_noException() {
        String exactlyTwentyChars = "a".repeat(20);
        assertDoesNotThrow(() -> validator.validate(exactlyTwentyChars));
    }
}
