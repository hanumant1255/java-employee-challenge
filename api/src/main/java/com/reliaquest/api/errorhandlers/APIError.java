package com.reliaquest.api.errorhandlers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class APIError {
    private String status;
    private String timestamp;
    private String id;
    private String message;
    Map<String, String> errors;
}
