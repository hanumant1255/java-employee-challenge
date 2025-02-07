package com.reliaquest.api.errorhandlers;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
public class APIException extends RuntimeException {

    private String msgId;
    private Object[] params;
    private HttpStatusCode httpStatusCode;

    public APIException(String msgId, Object[] params, HttpStatusCode httpStatusCode) {
        super();
        this.msgId = msgId;
        this.params = params;
        this.httpStatusCode = httpStatusCode;
    }
}
