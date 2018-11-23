package com.qmaker.survey.core.engines;

import com.qmaker.survey.core.entities.PushOrder;

public interface PushResponse {
    int CODE_DEFAULT_SUCCESS = 200, CODE_DEFAULT_FAILED = 400;

    String getMessage();

    int getCode();

    PushOrder getContent();
}
