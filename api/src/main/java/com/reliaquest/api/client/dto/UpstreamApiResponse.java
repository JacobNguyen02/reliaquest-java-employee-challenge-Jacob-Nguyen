package com.reliaquest.api.client.dto;

import lombok.Data;

@Data
public class UpstreamApiResponse<T> {
    private T data;
    private String status;
}
