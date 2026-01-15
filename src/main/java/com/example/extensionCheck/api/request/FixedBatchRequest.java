package com.example.extensionCheck.api.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FixedBatchRequest {
    private List<String> checked;
    private List<String> unchecked;
}
