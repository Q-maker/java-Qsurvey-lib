package com.qmaker.survey.core.interfaces;

public interface PushProcess {
    int STATE_SUCCESS = 0,
            STATE_ERROR = 1,
            STATE_FAILED = 2,
            STATE_FINISHED = 3,
            STATE_ABORTED = 4,
            STATE_STARTING = 5,
            STATE_RUNNING = 6,
            STATE_PENDING = 7;

    boolean cancel();

    int getState();
}
