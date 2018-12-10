package com.qmaker.survey.core.engines;

import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.interfaces.PushProcess;
import com.qmaker.survey.core.interfaces.Pusher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public final class PushExecutor {
    final List<String> managedTaskIds = Collections.synchronizedList(new ArrayList<String>());
    final List<Task> pendingTasks = Collections.synchronizedList(new ArrayList<Task>());
    final List<Task> processingTasks = Collections.synchronizedList(new ArrayList<Task>());
    final List<ExecutionStateChangeListener> listeners = Collections.synchronizedList(new ArrayList<ExecutionStateChangeListener>());
    boolean running = false, paused = false;

    PushExecutor() {
    }

    public boolean start() {
        if (running && !paused /*|| pendingTasks.isEmpty()*/) {
            return false;
        }
        this.paused = false;
        this.running = true;
        executeNext();
        return false;
    }

    public boolean pause() {
        this.paused = true;
        return this.running;
    }

    public boolean isStopped() {
        return !running;
    }

    public boolean isPaused() {
        return paused;
    }

    public int stop() {
        this.running = false;
        int stoppedCount = 0;
        for (Task task : processingTasks) {
            if (task.cancel()) {
                stoppedCount++;
            }
        }
        return stoppedCount;
    }

    public int[] cancel() {
        int[] out = {stop(), pendingTasks.size()};
        pendingTasks.clear();
        listeners.clear();
        return out;
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

    public Task getPendingTask(int index) {
        return pendingTasks.get(index);
    }

    public Task getProcessingTask(int index) {
        return processingTasks.get(index);
    }

    public int getPendingTaskCount() {
        return pendingTasks.size();
    }

    public int getProcessingTaskCount() {
        return processingTasks.size();
    }

    public List<Task> getManagedTasks() {
        List<Task> tasks = new ArrayList<>(processingTasks);
        tasks.addAll(pendingTasks);
        return tasks;
    }

    public Task enqueue(PushOrder order) {
        return enqueue(-1, order);
    }

    public Task enqueue(int priority, PushOrder order) {
        if (order == null) {
            return null;
        }
        if (managedTaskIds.contains(order.getId())) {
            Task retrievedTask = null;
            List<Task> tasks = getManagedTasks();
            for (Task t : tasks) {
                if (t.getId().equals(order.getId())) {
                    retrievedTask = t;
                    break;
                }
            }
            if (retrievedTask != null) {
                if (processingTasks.contains(retrievedTask)) {
                    reorderList(processingTasks, retrievedTask, priority);
                } else if (pendingTasks.contains(retrievedTask)) {
                    reorderList(pendingTasks, retrievedTask, priority);
                }
                autoExecuteIfNeeded();
                return retrievedTask;
            }

        }
        synchronized (pendingTasks) {
            if (priority < 0 || priority >= pendingTasks.size()) {
                priority = -1;
            }
            Task task = new Task(order);
            if (priority >= 0) {
                pendingTasks.add(priority, task);
            } else {
                pendingTasks.add(task);
            }
            synchronized (managedTaskIds) {
                managedTaskIds.add(task.getId());
            }
            dispatchTaskStateChanged(task);
            autoExecuteIfNeeded();
            return task;
        }
    }

    private void autoExecuteIfNeeded() {
        if (processingTasks.isEmpty()) {
            if (isRunning()) {
                executeNext();
            }
        }
    }

    private void reorderList(List<Task> pendingTasks, Task retrievedTask, int priority) {
        synchronized (pendingTasks) {
            int fromIndex = pendingTasks.indexOf(retrievedTask);
            if (fromIndex < 0) {
                return;
            }
            Collections.swap(pendingTasks, fromIndex, priority);
        }
    }

    private boolean execute(Task task, Pusher.Callback callback) {
        if (processingTasks.contains(task)) {
            return false;
        }
        PushOrder order = task.getOrder();
        order.setState(PushOrder.STATE_STARTING);
        dispatchTaskStateChanged(task);
        Pusher pusher = getPusher(order);
        callback = createInternalChainCallback(task, callback);
        if (pusher == null) {
            task.notifyCanNotProceed();
            callback.onFailed(new RuntimeException("No pusher found for given Order with id=" + task.getOrder().getId()));
        }
        synchronized (processingTasks) {
            processingTasks.add(task);
        }
        synchronized (pendingTasks) {
            pendingTasks.add(task);
        }
        try {
            task.attachTo(pusher.push(task.getOrder(), callback));
            return true;
        } catch (Exception e) {
            task.notifyCanNotProceed();
            return false;
        }
    }

    private Pusher.Callback createInternalChainCallback(final Task task, final Pusher.Callback callback) {
        return new Pusher.Callback() {
            @Override
            public void onSuccess(PushResult result) {
                dispatchPushFinishState(task);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(PushError result) {
                dispatchPushFinishState(task);
                if (callback != null) {
                    callback.onError(result);
                }
            }

            @Override
            public void onFailed(Throwable error) {
                dispatchPushFinishState(task);
                if (callback != null) {
                    callback.onFailed(error);
                }
            }

            @Override
            public void onFinish(int state) {
                pendingTasks.remove(task);
                processingTasks.remove(task);
                managedTaskIds.remove(task.getId());
                if (callback != null) {
                    callback.onFinish(state);
                }
            }
        };
    }

    private void dispatchPushFinishState(final Task task) {
        if (task.getOrder() != null) {
            task.getOrder().setState(task.getState());
        }
        synchronized (processingTasks) {
            processingTasks.remove(task);
        }
        synchronized (managedTaskIds) {
            managedTaskIds.remove(task.getId());
        }
        dispatchTaskStateChanged(task);
    }


    private void dispatchTaskStateChanged(final Task task) {
        dispatchTaskStateChanged(task, new Callable<ExecutionStateChangeListener[]>() {
            @Override
            public ExecutionStateChangeListener[] call() {
                return collectListeners();
            }
        });
    }

    private void dispatchTaskStateChanged(final Task task, final Callable<ExecutionStateChangeListener[]> listenerCallable) {
        if (task == null) {
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ExecutionStateChangeListener[] listeners = listenerCallable.call();
                    if (listeners == null) {
                        return;
                    }
                    for (ExecutionStateChangeListener listener : listeners) {
                        listener.onTaskStateChanged(task);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        QSurvey.getRunnableDispatcher().dispatch(runnable, 0);
    }

    private boolean executeNext() {
        if (pendingTasks.isEmpty()) {
            return false;
        }
        final Task nextTask = pendingTasks.get(0);
        execute(nextTask, createInternalChainCallback(nextTask, new Pusher.Callback() {
            @Override
            public void onSuccess(PushResult result) {

            }

            @Override
            public void onError(PushError result) {

            }

            @Override
            public void onFailed(Throwable error) {

            }

            @Override
            public void onFinish(int state) {
                if (!pendingTasks.isEmpty()) {
                    executeNext();
                }
            }
        }));
        return true;
    }

    private Pusher getPusher(PushOrder order) {
        return QSurvey.getPusher(order);
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

    public List<Task> enqueue(List<PushOrder> orders) {
        List<Task> tasks = new ArrayList();
        for (PushOrder order : orders) {
            tasks.add(enqueue(order));
        }
        return tasks;
    }

    public List<Task> enqueue(PushOrder... orders) {
        List<Task> tasks = new ArrayList();
        for (PushOrder order : orders) {
            tasks.add(enqueue(order));
        }
        return tasks;
    }

    public List<Task> enqueue(int priority, List<PushOrder> orders) {
        List<Task> tasks = new ArrayList();
        for (PushOrder order : orders) {
            tasks.add(enqueue(priority, order));
        }
        return tasks;
    }

    public List<Task> enqueue(int priority, PushOrder... orders) {
        List<Task> tasks = new ArrayList();
        for (PushOrder order : orders) {
            tasks.add(enqueue(priority, order));
        }
        return tasks;
    }

//    public void execute(List<PushOrder> orders, final ExecutionStateChangeListener listener) {
//        final Callable<ExecutionStateChangeListener[]> listeners = new Callable<ExecutionStateChangeListener[]>() {
//            @Override
//            public ExecutionStateChangeListener[] call() {
//                return new ExecutionStateChangeListener[]{listener};
//            }
//        };
//        List<Task> tasks = enqueue(orders);
//        for (final Task task : tasks) {
//            execute(task, new Pusher.Callback() {
//                @Override
//                public void onSuccess(PushResult result) {
//                    dispatchTaskStateChanged(task, listeners);
//                }
//
//                @Override
//                public void onError(PushError result) {
//                    dispatchTaskStateChanged(task, listeners);
//                }
//
//                @Override
//                public void onFailed(Throwable error) {
//                    dispatchTaskStateChanged(task, listeners);
//                }
//
//                @Override
//                public void onFinish(int state) {
//
//                }
//            });
//        }
//    }

    public static class Task {
        PushOrder order;
        PushProcess process;

        Task(PushOrder order) {
            this.order = order;
            this.order.setState(PushOrder.STATE_PENDING);
        }

        public int getState() {
            if (process != null) {
                return process.getState();
            }
            return order.getState();
        }

        public boolean isTerminated() {
            int state = getState();
            return process != null &&
                    (state == PushProcess.STATE_SUCCESS ||
                            state == PushProcess.STATE_FAILED ||
                            state == PushProcess.STATE_ERROR ||
                            state == PushProcess.STATE_ABORTED);
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
            if (process != null) {
                order.setState(PushOrder.STATE_PROCESSING);
            }
        }

        public void notifyCanNotProceed() {
            order.setState(PushOrder.STATE_CAN_NOT_PROCEED);
        }
    }

    public interface ExecutionStateChangeListener {
        void onTaskStateChanged(Task task);
    }
}
