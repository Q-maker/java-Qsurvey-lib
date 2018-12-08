package com.qmaker.survey.core.engines;

import com.qmaker.core.engines.QRunner;
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
import com.qmaker.survey.core.utils.MemoryPersistenceUnit;
import com.qmaker.survey.core.utils.PayLoad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import istat.android.base.tools.TextUtils;

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
            dispatchSurveyStateChanged(SurveyStateListener.STATE_PREPARED, runningSurvey, test);
        }
        return false;
    }

    @Override
    public boolean onRunnerTimeTick(QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_PREPARED, runningSurvey, test);
        }
        return false;
    }

    @Override
    public boolean onRunningExerciseChanged(QPackage qPackage, Test test, Exercise exercise, int exerciseIndex) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_PREPARED, runningSurvey, test, exercise, exerciseIndex);
        }
        return false;
    }

    @Override
    public boolean onRunningTimeOut(QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_PREPARED, runningSurvey, test);
        }
        return false;
    }

    @Override
    public boolean onRunCanceled(QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            dispatchSurveyStateChanged(SurveyStateListener.STATE_PREPARED, runningSurvey, test);
        }
        return false;
    }

    @Override
    public boolean onFinishRunning(QPackage qPackage, Test test) {
        if (runningSurvey != null && runningSurvey.getQPackage().getUriString().equals(qPackage.getUriString())) {
            Survey.Result result = runningSurvey.getResult(test);
            dispatchSurveyStateChanged(SurveyStateListener.STATE_FINISH, runningSurvey, result);
            List<PushOrder> orders = handleSurveyResultAsPushOrder(result);
            publishOrder(runningSurvey, orders);
            return Survey.TYPE_SYNCHRONOUS.equals(runningSurvey.getType());
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
            if (!Survey.TYPE_SYNCHRONOUS.equals(survey.getType())) {
                getPushExecutor().enqueue(orders);
            } else {
                getPushExecutor().enqueue(0, orders);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPushCautionUI(Survey survey, List<PushOrder> orders) {
        //TODO écrire le nécessaire pour faire sortir une UI indépendante qui éffectuera les execution chainé.
        /*
            Le problème est que le core ne sait pas quel type de UI lancer.
         */
    }

    private void dispatchSurveyStateChanged(int state, Survey survey, Object... vars) {
        PayLoad payLoad = new PayLoad(vars);
        synchronized (listeners) {
            for (SurveyStateListener listener : listeners) {
                if (listener != null) {
                    listener.onSurveyStateChanged(state, survey, payLoad);
                }
            }
        }
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

    public boolean unregisterRunStateListener(SurveyStateListener listener) {
        synchronized (listeners) {
            if (listeners.contains(listener)) {
                return false;
            }
            listeners.add(listener);
        }
        return false;
    }

    public final static RunnableDispatcher DEFAULT_RUNNABLE_DISPATCHER = new RunnableDispatcher() {
        @Override
        public void dispatch(Runnable runnable, int delay) {
            if (runnable != null) {
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                runnable.run();
            }
        }

        @Override
        public void cancel(Runnable runnable) {

        }

        @Override
        public void release() {

        }
    };
    static RunnableDispatcher defaultRunnableDispatcher = DEFAULT_RUNNABLE_DISPATCHER;

    public static void setDefaultRunnableDispatcher(RunnableDispatcher defaultRunnableDispatcher) {
        QSurvey.defaultRunnableDispatcher = defaultRunnableDispatcher != null ? defaultRunnableDispatcher : DEFAULT_RUNNABLE_DISPATCHER;
    }

    public static RunnableDispatcher getDefaultRunnableDispatcher() {
        return defaultRunnableDispatcher;
    }

    @Override
    public void onTaskStateChanged(PushExecutor.Task task) {
        if (persistenceUnit != null && task != null && task.isTerminated()) {
            if (task.getState() == PushProcess.STATE_SUCCESS) {
                persistenceUnit.delete(task.getOrder());
            } else {
                persistenceUnit.persist(task.getOrder());
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
//                    state != PushProcess.STATE_STARTING) {
//                persistenceUnit.persist(task.getOrder());
//            }
//        }
//    }

    public interface SurveyStateListener {
        int STATE_PREPARED = 0,
                STATE_STARTED = 1,
                STATE_TIME_TICK = 2,
                STATE_EXERCISE_CHANGED = 3,
                STATE_TIME_OUT = 4,
                STATE_CANCELED = 5,
                STATE_RESET = 6,
                STATE_FINISH = 7;
//        boolean onSurveyPrepared(Survey survey);
//
//        void onSurveyStarted(Survey survey, Test test);
//
//        void onSurveyTimeTick(Survey survey, Test test);
//
//        void onSurveyExerciseChanged(Survey survey, Test test, Exercise exercise, int exerciseIndex);
//
//        void onSurveyTimeOut(Survey survey, Test test);
//
//        void onSurveyCanceled(Survey survey, Test test);
//
//        void onSurveyReset(Survey survey, Test test);

        void onSurveyStateChanged(int state, Survey survey, PayLoad payLoad);
    }

    final static HashMap<String, Pusher> pusherMap = new HashMap();

    public static Pusher appendPusher(Pusher typeHandler) {
        if (typeHandler == null || TextUtils.isEmpty(typeHandler.getSupportedGrandType())) {
            return null;
        }
        return pusherMap.put(typeHandler.getSupportedGrandType(), typeHandler);
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
//        appendPusher(Pusher.DEFAULT);
//        appendPusher(Pusher.PUT_IN_ORDER);
//        appendPusher(Pusher.TYPE_MATCH_COLUMN);
//        appendPusher(Pusher.FILL_IN_THE_BLANK);
    }

    public static void resetPusher() {
        pusherMap.clear();
        resetDefaultPusher();
    }

}
