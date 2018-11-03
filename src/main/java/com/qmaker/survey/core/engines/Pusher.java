package com.qmaker.survey.core.engines;

import com.qmaker.core.entities.CopySheet;
import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.process.PushError;
import com.qmaker.survey.core.process.PushProcess;
import com.qmaker.survey.core.process.PushResponse;
import com.qmaker.survey.core.process.PushResult;

public abstract class Pusher {
    public final PushProcess push(CopySheet copySheet, Pusher.Callback callback) {
        return push(new PushOrder(copySheet, null), callback);
    }

    public final PushProcess push(PushOrder pushOrder, Pusher.Callback callback) {
        return null;
    }

    protected void notifySucces(PushResult result) {

    }

    protected void notifyError(PushError error) {

    }

    protected void notifyFailed(Exception e) {

    }

    public abstract String getSupportedGrandType();


    //TODO determiner les method offerte par ce callback.
    public interface Callback {

        void onStart(PushOrder order);

        void onCompleted(PushOrder order, PushResponse response);

        void onSucceed(PushResult result);

        void onError(PushError error);
    }
}
