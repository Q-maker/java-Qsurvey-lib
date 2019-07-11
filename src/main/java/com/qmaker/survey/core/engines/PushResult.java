package com.qmaker.survey.core.engines;

import com.qmaker.survey.core.entities.PushOrder;

//TODO reflechir au bien fonder de retourner l'ordre envoyé.
public class PushResult implements PushResponse {
    private final PushOrder source;
    String message;
    int code = CODE_DEFAULT_SUCCESS;

    public PushResult(PushOrder source) {
        this("success", CODE_DEFAULT_SUCCESS, source);
    }

    public PushResult(String message, int code, PushOrder source) {
        this.message = message;
        this.code = code;
        this.source = source;
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
    public PushOrder getSource() {
        return source;
    }
}
