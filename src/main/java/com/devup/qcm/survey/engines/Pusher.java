package com.devup.qcm.survey.engines;

import com.devup.qcm.core.entities.CopySheet;
import com.devup.qcm.survey.process.PushProcess;

public abstract class Pusher {

    public final PushProcess push(CopySheet copySheet, Pusher.Callback callback) {
        return null;
    }


    //TODO determiner les method offerte par ce callback.
    public interface Callback {
    }
}
