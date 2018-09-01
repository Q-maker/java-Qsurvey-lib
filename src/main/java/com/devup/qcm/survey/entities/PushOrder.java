package com.devup.qcm.survey.entities;

import com.devup.qcm.core.entities.CopySheet;

public class PushOrder {
    public final static String TAG = "push_order";
    String id;
    long createAt = System.currentTimeMillis();
    long doneAt;
    boolean done = false;
    CopySheet copySheet;

    public PushOrder() {

    }

}
