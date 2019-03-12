package com.qmaker.survey.core.engines;

import com.qmaker.core.engines.QRunner;
import com.qmaker.core.engines.Qmaker;
import com.qmaker.core.entities.Exercise;
import com.qmaker.core.entities.Test;
import com.qmaker.core.interfaces.RunnableDispatcher;
import com.qmaker.core.io.QPackage;
import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.entities.Repository;
import com.qmaker.survey.core.entities.Survey;
import com.qmaker.survey.core.interfaces.PersistenceUnit;
import com.qmaker.survey.core.interfaces.PushProcess;
import com.qmaker.survey.core.interfaces.Pusher;
import com.qmaker.survey.core.interfaces.SurveyStateListener;
import com.qmaker.survey.core.utils.MemoryPersistenceUnit;
import com.qmaker.survey.core.utils.PayLoad;
import com.qmaker.survey.core.pushers.FileIoPusher;
import com.qmaker.survey.core.pushers.HttpBasicPusher;
import com.qmaker.survey.core.pushers.HttpDigestPusher;
import com.qmaker.survey.core.pushers.JwtPusher;
import com.qmaker.survey.core.pushers.MemoryPusher;
import com.qmaker.survey.core.pushers.WssePusher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import istat.android.base.tools.TextUtils;

/**
 * @author Toukea Tatsi J
 */
public class QSurvey implements QRunner.StateListener, PushExecutor.ExecutionStateChangeListener {
    static QSurvey instance;
    final List<SurveyStateListener> listeners = new ArrayList<>();
    PersistenceUnit persistenceUnit = new MemoryPersistenceUnit();
    final PushExecutor pushExecutor = new PushExecutor();
    Survey runningSurvey;

    private QSurvey() {
        pushExecutor.registerExecutionStateChangeListener(this);
        pushExecutor.start();
        resetDefaultPusher();
    }

    public Survey getRunningSurvey() {
        return runningSurvey;
    }

    public PushExecutor getPushExecutor() {
        return pushExecutor;
    }

    public static QSurvey getInstance(boolean initializeIfNeeded) {
        if (!initializeIfNeeded) {
            return getInstance();
        }
        if (isInitialized()) {
            return getInstance();
        } else {
            return initialize();
        }
    }

    public static QSurvey getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AndroidQSurvey was not initialised.");
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static QSurvey initialize() {
        if (instance != null) {
            throw new IllegalStateException("AndroidQSurvey instance is already initialized and is ready do be get using getInstance");
        }
        instance = new QSurvey();
        instance.init();
        return instance;
    }


    private void init() {
        QRunner.getInstance().registerStateListener(0, this);
    }

    public QSurvey usePersistenceUnit(PersistenceUnit pUnit) {
        this.persistenceUnit = pUnit;
        return this;
    }

    @Override
    public boolean onRunnerPrepare(String uri) {
        return false;
    }

    @Override
    public boolean onRunnerPrepared(QRunner.PrepareResult result) {
        try {
            runningSurvey = Survey.from(result.getTarget());
            dispatchSurveyStateChanged(SurveyStateListener.STATE_PREPARED, runningSurvey);
        } catch (Survey.InvalidSurveyException e) {
            e.printStackTrace();
            //Nothing to do, qpackake is not a survey.
        } catch (Exception e) {
            e.printStackTrace();
            //une exception innatentdu est survenu.
        }
        return false;
    }

    @Override
    public boolean onStartRunning(QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_STARTED, runningSurvey, test);
        }
        return false;
    }

    @Override
    public boolean onRunnerTimeTick(QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_TIME_TICK, runningSurvey, test);
        }
        return false;
    }

    @Override
    public boolean onRunningExerciseChanged(QPackage qPackage, Test test, Exercise exercise, int exerciseIndex) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_EXERCISE_CHANGED, runningSurvey, test, exercise, exerciseIndex);
        }
        return false;
    }

    @Override
    public boolean onRunningTimeOut(QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_TIME_OUT, runningSurvey, test);
        }
        return false;
    }

    @Override
    public boolean onRunnerPaused(QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_PAUSED, runningSurvey, test);
        }
        return false;
    }

    @Override
    public boolean onRunCanceled(QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_CANCELED, runningSurvey, test);
        }
        return false;
    }

    @Override
    public boolean onFinishRunning(QPackage qPackage, Test test) {
        if (notifySurveyCompleted(SurveyStateListener.STATE_COMPLETED, qPackage, test)) {
            return true;
        }
        return false;
    }

    private boolean notifySurveyCompleted(int state, QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            Survey.Result result = runningSurvey.getResult(test);
            List<PushOrder> orders = handleSurveyResultAsPushOrder(result);
            dispatchSurveyStateChanged(state, runningSurvey, result, orders);
            publishOrder(runningSurvey, orders);
            //TODO reflechir si il est pas mieux de toute façon de prendre le controle total ce qui permetrait de dérer si le replay est possible avec les autre configuration de la survey
            return runningSurvey.isBlockingPublisherNeeded();
        }
        return false;
    }

    private List<PushOrder> handleSurveyResultAsPushOrder(Survey.Result result) {
        List<Repository> repositories = result.getOrigin().getRepositories();
        List<PushOrder> out = new ArrayList<>();
        PushOrder order;
        for (Repository repo : repositories) {
            order = new PushOrder(result.getCopySheet(), repo);
            out.add(order);
            if (persistenceUnit != null) {
                persistenceUnit.persist(order);
            }
        }
        return out;
    }

    private void publishOrder(Survey survey, List<PushOrder> orders) {
        try {
            if (survey.isBlockingPublisherNeeded()) {
                getPushExecutor().enqueue(0, orders);
            } else {
                getPushExecutor().enqueue(orders);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatchSurveyStateChanged(final int state, final Survey survey, final Object... vars) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                PayLoad payLoad = new PayLoad(vars);
                synchronized (listeners) {
                    for (SurveyStateListener listener : listeners) {
                        if (listener != null) {
                            listener.onSurveyStateChanged(state, survey, payLoad);
                        }
                    }
                }
            }
        };
        runnableDispatcher.dispatch(runnable, 0);
    }

    /**
     * Execute all waiting PushOrder still present into the PersistenceUnit.
     *
     * @return
     */
    public List<PushExecutor.Task> syncResults() {
        if (persistenceUnit == null) {
            return new ArrayList();
        }
        List<PushOrder> orders = persistenceUnit.findAll();
        return getPushExecutor().enqueue(orders);
    }

    @Override
    public boolean onResetRunner(QPackage qPackage, Test test) {
        return false;
    }

    public boolean registerSurveyStateListener(SurveyStateListener listener) {
        return registerSurveyStateListener(-1, listener);
    }

    public boolean registerSurveyStateListener(int priority, SurveyStateListener listener) {
        synchronized (listeners) {
            if (listeners.contains(listener)) {
                return false;
            }
            if (priority >= 0 && priority < listeners.size() - 1) {
                listeners.add(priority, listener);
            } else {
                listeners.add(listener);
            }
        }
        return true;
    }

    public boolean unregisterSurveyStateListener(SurveyStateListener listener) {
        synchronized (listeners) {
            if (listeners.contains(listener)) {
                return false;
            }
            listeners.add(listener);
        }
        return false;
    }

    static RunnableDispatcher runnableDispatcher = Qmaker.getDefaultRunnableDispatcher();

    public static void setRunnableDispatcher(RunnableDispatcher runnableDispatcher) {
        QSurvey.runnableDispatcher = runnableDispatcher != null ? runnableDispatcher : Qmaker.getDefaultRunnableDispatcher();
    }

    public static RunnableDispatcher getRunnableDispatcher() {
        return runnableDispatcher;
    }

    @Override
    public void onTaskStateChanged(PushExecutor.Task task) {
        if (task.isTerminated()) {
            if (persistenceUnit != null && task != null) {
                if (task.getState() == PushProcess.STATE_SUCCESS) {
                    persistenceUnit.delete(task.getOrder());
                } else {
                    persistenceUnit.persist(task.getOrder());
                }
            }
        }
    }

//    public void onTaskStateChanged(PushExecutor.Task task) {
//        if (persistenceUnit != null) {
//            int state = task.getState();
//            if (state == PushProcess.STATE_SUCCESS) {
//                persistenceUnit.delete(task.getOrder());
//            } else if (state != PushProcess.STATE_PROCESSING &&
//                    state != PushProcess.STATE_PENDING &&
//                    state != PushProcess.STATE_STARTED) {
//                persistenceUnit.persist(task.getOrder());
//            }
//        }
//    }

    final static HashMap<String, Pusher> pusherMap = new HashMap();

    public static Pusher appendPusher(Pusher pusher) {
        if (pusher == null || TextUtils.isEmpty(pusher.getSupportedGrandType())) {
            return null;
        }
        return pusherMap.put(pusher.getSupportedGrandType(), pusher);
    }

    public static Pusher removePusher(String typeName) {
        return pusherMap.remove(typeName);
    }

    static Pusher getPusher(String grandType) {
        Pusher pusher = pusherMap.get(grandType);
        return pusher;
    }


    static Pusher getPusher(PushOrder order) {
        if (order == null || order.getRepository() == null) {
            return null;
        }
        return getPusher(order.getRepository().getGrandType());
    }

    //TODO ajouter des pusher par défaut a partir d'ici.
    public static void resetDefaultPusher() {
        appendPusher(new HttpBasicPusher());
        appendPusher(new JwtPusher());
        appendPusher(new HttpDigestPusher());
        appendPusher(new MemoryPusher());
        appendPusher(new WssePusher());
        try {
            appendPusher(new FileIoPusher());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetPusher() {
        pusherMap.clear();
        resetDefaultPusher();
    }

}
