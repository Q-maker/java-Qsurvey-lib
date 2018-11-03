package com.qmaker.survey.core.process;

import com.qmaker.survey.core.entities.PushOrder;

public class PushResult implements PushResponse {
    private final PushOrder content;
    PushOrder order;
    String message;
    int code = CODE_DEFAULT_SUCCESS;

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
        return order;
    }
}
