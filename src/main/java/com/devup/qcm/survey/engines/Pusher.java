package com.devup.qcm.survey.engines;

import com.devup.qcm.core.entities.CopySheet;
import com.devup.qcm.survey.entities.PushOrder;
import com.devup.qcm.survey.process.PushError;
import com.devup.qcm.survey.process.PushProcess;
import com.devup.qcm.survey.process.PushResponse;
import com.devup.qcm.survey.process.PushResult;

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
