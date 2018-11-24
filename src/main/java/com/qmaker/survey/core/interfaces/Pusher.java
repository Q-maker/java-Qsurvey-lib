package com.qmaker.survey.core.interfaces;

import com.qmaker.survey.core.engines.PushError;
import com.qmaker.survey.core.engines.PushResult;
import com.qmaker.survey.core.entities.PushOrder;

public interface Pusher {

    PushProcess push(PushOrder order, Pusher.Callback callback);

    interface Callback {

        void onSuccess(PushResult result);

        void onError(PushError result);

        void onFailed(Throwable error);

        void onFinish(int state);

    }
}
