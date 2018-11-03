package com.qmaker.survey.core.process;

import com.qmaker.survey.core.entities.PushOrder;

public interface PushResponse {
    int CODE_DEFAULT_SUCCESS = 200, CODE_DEFAULT_FAILD = 400;

    String getMessage();

    int getCode();

    PushOrder getContent();
}
