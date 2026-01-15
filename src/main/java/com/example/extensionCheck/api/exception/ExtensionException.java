package com.example.extensionCheck.api.exception;

import lombok.Getter;

@Getter
public class ExtensionException extends RuntimeException {

    private final ExtensionErrorCode errorCode;

    public ExtensionException(ExtensionErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
