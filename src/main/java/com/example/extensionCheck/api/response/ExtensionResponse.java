package com.example.extensionCheck.api.response;

import com.example.extensionCheck.entity.ExtensionType;
import com.example.extensionCheck.entity.Extensions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ExtensionResponse {

    private Long id;
    private String name;
    private ExtensionType type;
    private boolean isActive;

    public static ExtensionResponse from(Extensions extension) {
        return ExtensionResponse.builder()
                .id(extension.getId())
                .name(extension.getName())
                .type(extension.getType())
                .isActive(extension.isActive())
                .build();
    }
}
