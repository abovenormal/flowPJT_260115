package com.example.extensionCheck.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ExtensionListResponse {

    private List<String> fixed;
    private List<String> custom;
    private int count;
    private String type;  // "full" 또는 "delta"

    public static ExtensionListResponse full(List<String> fixed, List<String> custom) {
        return ExtensionListResponse.builder()
                .fixed(fixed)
                .custom(custom)
                .count(custom.size())
                .type("full")
                .build();
    }

    public static ExtensionListResponse delta(List<String> fixedAdded, List<String> fixedRemoved) {
        return ExtensionListResponse.builder()
                .fixed(fixedAdded)
                .custom(fixedRemoved)
                .type("delta")
                .build();
    }
}
