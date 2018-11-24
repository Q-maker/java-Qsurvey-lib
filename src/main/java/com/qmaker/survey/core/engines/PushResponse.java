package com.qmaker.survey.core.engines;


public interface PushResponse {
    int CODE_DEFAULT_SUCCESS = 200,
            CODE_DEFAULT_ERROR = 400,
            CODE_FAILED = -1;

    String getMessage();

    int getCode();

    <T> T getContent();
}
