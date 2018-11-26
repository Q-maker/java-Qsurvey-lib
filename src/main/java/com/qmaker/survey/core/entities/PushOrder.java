package com.qmaker.survey.core.entities;

import com.qmaker.core.entities.CopySheet;

import java.util.ArrayList;
import java.util.List;

public class PushOrder {
    public final static int STATE_DONE = 0,
            STATE_ERROR = 1,
            STATE_FAILED = 2,
            STATE_FINISHED = 3,
            STATE_ABORTED = 4,
            STATE_STARTING = 5,
            STATE_PROCESSING = 6,
            STATE_PENDING = 7,
            STATE_LATENT = 8;
    public final static String TAG = "pushOrder";
    String id;
    long createAt = System.currentTimeMillis();
    long lastModifiedAt = createAt;
    long doneAt;
    int state = STATE_LATENT;
    CopySheet copySheet;
    Repository repository;

    public PushOrder(CopySheet copySheet, Repository auth) {
        this.copySheet = copySheet;
        this.repository = auth;
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

    public void setState(int state) {
        this.state = state;
    }

    public CopySheet getCopySheet() {
        return copySheet;
    }

    public Repository getRepository() {
        return repository;
    }

    public static List<PushOrder> listFrom(Survey survey, CopySheet copySheet) throws InstantiationException, IllegalAccessException {
        List<Repository> repositories = survey.getRepositories();
        List<PushOrder> out = new ArrayList<>();
        for (Repository repo : repositories) {
            out.add(new PushOrder(copySheet, repo));
        }
        return out;
    }
}
