package com.qmaker.survey.core.engines;

import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.interfaces.PushProcess;
import com.qmaker.survey.core.interfaces.Pusher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PushExecutor {
    final List<Task> pendingTasks = new ArrayList();
    final List<Task> processingTasks = new ArrayList();
    final List<ExecutionStateChangeListener> listeners = Collections.synchronizedList(new ArrayList<ExecutionStateChangeListener>());
    boolean running = false;

    public boolean start() {
        if (running || pendingTasks.isEmpty()) {
            return false;
        }
        this.running = true;
        executeNext();
        return false;
    }

    public boolean pause() {
        return false;
    }

    public boolean cancel() {
        return false;
    }

    public boolean isProcessing() {
        return !processingTasks.isEmpty();
    }

    public boolean isRunning() {
        return running;
    }

    public List<Task> getPendingTasks() {
        return pendingTasks;
    }

    public List<Task> getProcessingTasks() {
        return processingTasks;
    }

    public Task enqueue(PushOrder order) {
        return enqueue(-1, order);
    }

    public Task enqueue(int priority, PushOrder order) {
        if (priority < 0 || priority >= pendingTasks.size()) {
            priority = -1;
        }
        Task task = new Task(order);
        if (priority >= 0) {
            pendingTasks.add(priority, task);
        } else {
            pendingTasks.add(task);
        }
        return task;
    }

    private void execute(Task task, Pusher.Callback callback) {
        Pusher pusher = getPusher(task.getOrder());
        if (pusher == null && !pendingTasks.isEmpty()) {
            executeNext();
        }
        processingTasks.add(task);
        dispatchPushState(task);
        task.attachTo(pusher.push(task.getOrder(), createPushChainCallback(callback)));
    }

    private Pusher.Callback createInternalChainCallback(final Task task, final Pusher.Callback callback) {
        return new Pusher.Callback() {
            @Override
            public void onSuccess(PushResult result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
                dispatchPushState(task);
            }

            @Override
            public void onError(PushError result) {
                if (callback != null) {
                    callback.onError(result);
                }
                dispatchPushState(task);
            }

            @Override
            public void onFailed(Throwable error) {
                if (callback != null) {
                    callback.onFailed(error);
                }
                dispatchPushState(task);
            }

            @Override
            public void onFinish(int state) {
                if (callback != null) {
                    callback.onFinish(state);
                }
            }
        };
    }

    private void dispatchPushState(final Task task) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ExecutionStateChangeListener[] listeners = collectListeners();
                if (listeners == null) {
                    return;
                }
                for (ExecutionStateChangeListener listener : listeners) {
                    listener.onTaskStateChanged(task);
                }
            }
        };
        QSurvey.getDefaultRunnableDispatcher().dispatch(runnable, 0);
    }

    private Pusher.Callback createPushChainCallback(Pusher.Callback callback) {
        return null;
    }

    private void executeNext() {
        Task nextTask = pendingTasks.get(0);
        execute(nextTask, createInternalChainCallback(nextTask, null));
    }

    private Pusher getPusher(PushOrder order) {
        //TODO find the most suitable Pusher for this order.
        return null;
    }

    private ExecutionStateChangeListener[] collectListeners() {
        ExecutionStateChangeListener[] callbacks = null;
        synchronized (listeners) {
            if (listeners.size() > 0) {
                callbacks = new ExecutionStateChangeListener[listeners.size()];
                callbacks = listeners.toArray(callbacks);
            }
        }
        return callbacks;
    }

    public boolean registerExecutionStateChangeListener(ExecutionStateChangeListener listener) {
        return registerExecutionStateChangeListener(-1, listener);
    }

    public boolean registerExecutionStateChangeListener(int priority, ExecutionStateChangeListener listener) {
        synchronized (listeners) {
            if (listener == null || listeners.contains(listener)) {
                return false;
            }
            if (priority >= 0 && priority <= listeners.size() - 1) {
                listeners.add(priority, listener);
            } else {
                listeners.add(listener);
            }
        }
        return true;
    }

    public boolean unregisterExecutionStateChangeListener(ExecutionStateChangeListener listener) {
        synchronized (listeners) {
            return listener != null && listeners.remove(listener);
        }
    }

    public static class Task {
        PushOrder order;
        PushProcess process;

        public Task(PushOrder order) {
            this.order = order;
        }

        public int getState() {
            return process != null ? process.getState() : PushProcess.STATE_PENDING;
        }

        public PushOrder getOrder() {
            return order;
        }

        public String getId() {
            return order.getId();
        }

        public boolean cancel() {
            return process != null ? process.cancel() : false;
        }

        public PushResponse getResponse() {
            return process != null ? process.getResponse() : null;
        }

        public PushResult getResult() {
            PushResponse response = getResponse();
            if (response != null && response instanceof PushResult) {
                return (PushResult) response;
            }
            return null;
        }

        public PushError getError() {
            PushResponse response = getResponse();
            if (response != null && response instanceof PushResult) {
                return (PushError) response;
            }
            return null;
        }

        public Throwable getFailCause() {
            return process != null ? process.getFailCause() : null;
        }


        public void attachTo(PushProcess process) {
            this.process = process;
        }
    }

    public interface ExecutionStateChangeListener {
        void onTaskStateChanged(Task task);
    }
}
