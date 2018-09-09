package com.devup.qcm.survey.process;

import com.devup.qcm.core.entities.CopySheet;
import com.devup.qcm.survey.entities.PushOrder;

public interface PushResponse {
    int CODE_DEFAULT_SUCCESS = 200, CODE_DEFAULT_FAILD = 400;

    String getMessage();

    int getCode();

    PushOrder getContent();
}
