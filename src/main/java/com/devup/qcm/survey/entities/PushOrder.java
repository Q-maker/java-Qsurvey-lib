package com.devup.qcm.survey.entities;

import com.devup.qcm.core.entities.CopySheet;

public class PushOrder {
    final static int STATE_PENDING = 0,
            STATE_PROCESSING = 1,
            STATE_CANCELED = 2,
            STATE_FAILED = 3,
            STATE_DONE = 4;
    public final static String TAG = "push_order";
    String id;
    long createAt = System.currentTimeMillis();
    long lastModifiedAt = createAt;
    long doneAt;
    int state;
    CopySheet copySheet;

    public PushOrder(CopySheet copySheet) {
        this.copySheet = copySheet;
    }

    public void notifyModified() {
        lastModifiedAt = System.currentTimeMillis();
    }

    public void notifyDone() {
        this.doneAt = System.currentTimeMillis();
        this.state = STATE_DONE;
        notifyModified();
    }

    public String getId() {
        return id;
    }

    public long getCreateAt() {
        return createAt;
    }

    public long getLastModifiedAt() {
        return lastModifiedAt;
    }

    public long getDoneAt() {
        return doneAt;
    }

    public boolean isDone() {
        return state == STATE_DONE;
    }

    public int getState() {
        return state;
    }

    public CopySheet getCopySheet() {
        return copySheet;
    }
}
