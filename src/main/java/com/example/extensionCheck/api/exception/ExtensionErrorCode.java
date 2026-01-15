package com.example.extensionCheck.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExtensionErrorCode {
    EMPTY_INPUT("EXT_001", "확장자를 입력해주세요."),
    TOO_LONG("EXT_002", "확장자는 최대 20자까지 입력 가능합니다."),
    CONTAINS_DIGIT("EXT_003", "확장자에 숫자를 포함할 수 없습니다."),
    CONTAINS_KOREAN("EXT_004", "확장자에 한글을 포함할 수 없습니다."),
    ALREADY_EXISTS("EXT_005", "이미 등록된 확장자입니다."),
    NOT_FOUND("EXT_006", "확장자를 찾을 수 없습니다."),
    MAX_LIMIT_EXCEEDED("EXT_007", "커스텀 확장자는 최대 200개까지 등록할 수 있습니다."),
    FIXED_EXTENSION_CONFLICT("EXT_008", "해당 확장자는 고정 확장자로 등록되어 사용할 수 없습니다.");

    private final String code;
    private final String message;
}
