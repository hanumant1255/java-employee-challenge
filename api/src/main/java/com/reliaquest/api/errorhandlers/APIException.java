package com.reliaquest.api.errorhandlers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class APIException extends RuntimeException {

    private String msgId;
    private Object[] params;

    public APIException(String msgId, Object[] params) {
        super();
        this.msgId = msgId;
        this.params = params;
    }
}
