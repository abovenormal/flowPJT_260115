package com.example.extensionCheck.api.validator;

import com.example.extensionCheck.api.exception.ExtensionErrorCode;
import com.example.extensionCheck.api.exception.ExtensionException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ExtensionValidator {

    private static final int MAX_LENGTH = 20;
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern KOREAN_PATTERN = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]");

    public void validate(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new ExtensionException(ExtensionErrorCode.EMPTY_INPUT);
        }
        if (extension.length() > MAX_LENGTH) {
            throw new ExtensionException(ExtensionErrorCode.TOO_LONG);
        }
        if (DIGIT_PATTERN.matcher(extension).find()) {
            throw new ExtensionException(ExtensionErrorCode.CONTAINS_DIGIT);
        }
        if (KOREAN_PATTERN.matcher(extension).find()) {
            throw new ExtensionException(ExtensionErrorCode.CONTAINS_KOREAN);
        }
    }
}
