package com.qmaker.survey.core.engines;

import com.qmaker.survey.core.entities.PushOrder;

import java.util.ArrayList;
import java.util.List;

public class PushError extends Exception implements PushResponse {
    private final PushOrder content;
    int code = CODE_FAILED;
    List<PushError> subErrors = new ArrayList();

    public PushError(String message, int code, PushOrder content) {
        this(null, message, code, content);
    }

    public PushError(Throwable cause, PushOrder content) {
        super(cause);
        this.content = content;
    }

    public PushError(Throwable cause, String message, int code, PushOrder content) {
        super(message, cause);
        this.code = code;
        this.content = content;
    }

    public List<PushError> getSubErrors() {
        return subErrors;
    }

    public boolean hasSubErrors() {
        return subErrors != null && !subErrors.isEmpty();
    }

    public boolean isFaild() {
        return code < 0;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public PushOrder getSource() {
        return content;
    }
}
