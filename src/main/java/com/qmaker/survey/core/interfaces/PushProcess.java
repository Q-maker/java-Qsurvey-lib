package com.qmaker.survey.core.interfaces;

import com.qmaker.survey.core.engines.PushResponse;
import com.qmaker.survey.core.entities.PushOrder;

public interface PushProcess {
    int STATE_SUCCESS = PushOrder.STATE_SUCCESS,
            STATE_ERROR = PushOrder.STATE_ERROR,
            STATE_FAILED = PushOrder.STATE_FAILED,
            STATE_ABORTED = PushOrder.STATE_ABORTED,
            STATE_STARTING = PushOrder.STATE_STARTED,
            STATE_PROCESSING = PushOrder.STATE_PROCESSING,
            STATE_PENDING = PushOrder.STATE_PENDING;

    //TODO Choisir entre passer un callback et retourner un Boolean ou ne rien passer et retourner un promise.
    boolean proceed(PushOrder order, Pusher.Callback callback);

    boolean cancel();

    int getState();

    PushResponse getResponse();

    Throwable getFailCause();

    class AbortionException extends Exception {
        public AbortionException() {
            super("Process aborted");
        }
    }
}
