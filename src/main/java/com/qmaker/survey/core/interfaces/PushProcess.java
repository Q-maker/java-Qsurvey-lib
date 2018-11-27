package com.qmaker.survey.core.interfaces;

import com.qmaker.survey.core.engines.PushResponse;
import com.qmaker.survey.core.entities.PushOrder;

public interface PushProcess {
    int STATE_SUCCESS = PushOrder.STATE_DONE,
            STATE_ERROR = PushOrder.STATE_ERROR,
            STATE_FAILED = PushOrder.STATE_FAILED,
            STATE_ABORTED = PushOrder.STATE_ABORTED,
            STATE_STARTING = PushOrder.STATE_STARTING,
            STATE_PROCESSING = PushOrder.STATE_PROCESSING,
            STATE_PENDING = PushOrder.STATE_PENDING;

    boolean cancel();

    int getState();

    PushResponse getResponse();

    Throwable getFailCause();
}
