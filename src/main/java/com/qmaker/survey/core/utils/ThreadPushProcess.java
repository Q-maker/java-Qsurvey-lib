package com.qmaker.survey.core.utils;

import com.qmaker.core.engines.Qmaker;
import com.qmaker.core.interfaces.RunnableDispatcher;
import com.qmaker.survey.core.engines.PushError;
import com.qmaker.survey.core.engines.PushResponse;
import com.qmaker.survey.core.engines.PushResult;
import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.interfaces.PushProcess;
import com.qmaker.survey.core.interfaces.Pusher;

public abstract class ThreadPushProcess implements PushProcess, Runnable {
    Thread thread = new Thread(this);
    Pusher.Callback callback;
    PushOrder order;
    PushResponse response;
    Throwable failCause;
    RunnableDispatcher runnableDispatcher;
    int state = PushProcess.STATE_PENDING;

    public ThreadPushProcess() {
        this(null);
    }

    public ThreadPushProcess(RunnableDispatcher runnableDispatcher) {
        this.runnableDispatcher = runnableDispatcher != null ? runnableDispatcher : Qmaker.getDefaultRunnableDispatcher();
    }

    @Override
    public final void run() {
        try {
            run(order);
        } catch (Exception e) {
            notifyFailed(e);
        }
    }

    public final boolean proceed(PushOrder order, Pusher.Callback callback) {
        return onProceed(order, callback);
    }

    @Override
    public final boolean onProceed(PushOrder order, Pusher.Callback callback) {
        this.failCause = null;
        this.response = null;
        this.order = order;
        this.callback = callback;
        this.state = PushProcess.STATE_STARTING;
        thread.start();
        return true;
    }

    protected void notifySuccess(final PushResult result) {
        runnableDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    state = PushProcess.STATE_SUCCESS;
                    callback.onSuccess(result);
                    callback.onFinish(getState());
                }
            }
        }, 0);
    }

    protected void notifyFailed(final Throwable error) {
        runnableDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    state = error instanceof AbortionException ? PushProcess.STATE_ABORTED : PushProcess.STATE_FAILED;
                    callback.onFailed(error);
                    callback.onFinish(getState());
                }
            }
        }, 0);
    }

    protected void notifyError(final PushError error) {
        runnableDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    state = PushProcess.STATE_ERROR;
                    callback.onError(error);
                    callback.onFinish(getState());
                }
            }
        }, 0);
    }

    protected abstract void run(PushOrder order) throws Exception;

    @Override
    public boolean cancel() {
        if (thread.isAlive()) {
            thread.interrupt();
            notifyFailed(new AbortionException());
            return true;
        }
        return false;
    }

    @Override
    public int getState() {
        if (!thread.isAlive()) {
            return PushProcess.STATE_PENDING;
        }
        return state;
    }

    @Override
    public PushResponse getResponse() {
        return response;
    }

    @Override
    public Throwable getFailCause() {
        return failCause;
    }

    public boolean hasFaild() {
        return failCause != null;
    }

    public boolean hasError() {
        return response instanceof PushError;
    }

}
