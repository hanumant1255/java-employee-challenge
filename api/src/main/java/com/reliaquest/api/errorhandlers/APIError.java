package com.reliaquest.api.errorhandlers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class APIError {

    private String reason;

    private String id;

    private Object[] params;
}
