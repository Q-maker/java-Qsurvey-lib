package com.qmaker.survey.core.engines;

import com.qmaker.survey.core.entities.PushOrder;

//TODO reflechir au bien fonder de retourner l'ordre envoyé.
public class PushResult implements PushResponse {
    private final PushOrder content;
    String message;
    int code = CODE_DEFAULT_SUCCESS;

    public PushResult(PushOrder content) {
        this("success", CODE_DEFAULT_SUCCESS, content);
    }

    public PushResult(String message, int code, PushOrder content) {
        this.message = message;
        this.code = code;
        this.content = content;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public PushOrder getContent() {
        return content;
    }
}
