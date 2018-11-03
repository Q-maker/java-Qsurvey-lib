package com.qmaker.survey.core.entities;

import com.qmaker.core.entities.CopySheet;

import java.util.ArrayList;
import java.util.List;

public class PushOrder {
    final static int STATE_PENDING = 0,
            STATE_PROCESSING = 1,
            STATE_CANCELED = 2,
            STATE_FAILED = 3,
            STATE_DONE = 4;
    public final static String TAG = "pushOrder";
    String id;
    long createAt = System.currentTimeMillis();
    long lastModifiedAt = createAt;
    long doneAt;
    int state;
    CopySheet copySheet;
    Auth auth;

    public PushOrder(CopySheet copySheet, Auth auth) {
        this.copySheet = copySheet;
        this.auth = auth;
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

    public Auth getAuth() {
        return auth;
    }

    public static List<PushOrder> listFrom(Survey survey, CopySheet copySheet) throws InstantiationException, IllegalAccessException {
        List<Auth> authList = survey.getAuthList();
        List<PushOrder> out = new ArrayList<>();
        for (Auth auth : authList) {
            out.add(new PushOrder(copySheet, auth));
        }
        return out;
    }
}
