package com.qmaker.survey.core.interfaces;

import com.qmaker.survey.core.engines.PushError;
import com.qmaker.survey.core.engines.PushResult;
import com.qmaker.survey.core.entities.PushOrder;

//TODO penser a une possibilité de checker (a l'aide d1 HEAD) si le repository est available.
public interface Pusher {

    PushProcess push(PushOrder order, Pusher.Callback callback) throws Exception;

    String getSupportedGrandType();

    interface Callback {

        void onSuccess(PushResult result);

        void onError(PushError result);

        //TODO doit t'on vraiment prendre en cas les Failed.
        void onFailed(Throwable error);

        void onFinish(int state);

    }
}
