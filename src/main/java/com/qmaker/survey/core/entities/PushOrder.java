package com.qmaker.survey.core.entities;

import com.google.gson.Gson;
import com.qmaker.core.entities.CopySheet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.qmaker.core.utils.ToolKits.generateID;

public class PushOrder {
    public final static int
            STATE_LATENT = -1,
            STATE_STARTED = 7,
            STATE_PROCESSING = 31,
            STATE_SUCCESS = 255,
            STATE_ERROR = 127,
            STATE_FAILED = 111,
            STATE_ABORTED = 95,
            STATE_PENDING = 15,
            STATE_DROPPED = 1,
            STATE_FLAG_FINISHED = 65;
    public final static String TAG = "pushOrder";
    String id;
    long createAt = System.currentTimeMillis();
    long lastModifiedAt = createAt;
    long doneAt;
    int state = STATE_LATENT;
    CopySheet copySheet;
    Repository repository;
    String copySheetId;

    public PushOrder(CopySheet copySheet, Repository auth) {
        this.copySheet = copySheet;
        this.repository = auth;
        this.copySheetId = copySheet.getId();
        this.id = UUID.randomUUID().toString();
    }

    public void notifyModified() {
        lastModifiedAt = System.currentTimeMillis();
    }

    public void notifyDone() {
        this.doneAt = System.currentTimeMillis();
        this.state = STATE_SUCCESS;
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
        return state == STATE_SUCCESS;
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

    public static List<PushOrder> listFrom(Survey survey, CopySheet copySheet) {
        List<Repository> repositories = survey.getRepositories();
        List<PushOrder> out = new ArrayList<>();
        for (Repository repo : repositories) {
            out.add(new PushOrder(copySheet, repo));
        }
        return out;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
