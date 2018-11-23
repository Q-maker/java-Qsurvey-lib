package com.qmaker.survey.core.engines;

import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.interfaces.PushProcess;
import com.qmaker.survey.core.interfaces.Pusher;

import java.util.ArrayList;
import java.util.List;

public class PushExecutor {
    List<Task> pendingTasks = new ArrayList();

    private void execute(Task task, Pusher.Callback callback) {
        Pusher pusher = getPusher(task.getOrder());
        if (pusher == null && !pendingTasks.isEmpty()) {
            executeNext();
        }
        pusher.push(task.getOrder(), createPushChainCallback(callback));
    }

    private Pusher.Callback createPushChainCallback(Pusher.Callback callback) {
        //TODO  return un callback qui chainera le prochain executeNext lors du complete.
        return null;
    }

    private void executeNext() {
        Task nextTask = pendingTasks.get(0);
        execute(nextTask);
    }

    private Pusher getPusher(PushOrder order) {
        //TODO find the most suitable Pusher for this order.
        return null;
    }


    public static class Task {
        PushOrder order;
        PushProcess process;

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


    }
}
