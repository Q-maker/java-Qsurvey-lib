package com.devup.qcm.survey.engines;

import com.devup.qcm.core.engines.QRunner;
import com.devup.qcm.core.entities.CopySheet;
import com.devup.qcm.core.entities.Exercise;
import com.devup.qcm.core.entities.Test;
import com.devup.qcm.core.io.QPackage;
import com.devup.qcm.survey.entities.PushOrder;
import com.devup.qcm.survey.entities.Survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QSurvey implements QRunner.RunStateListener {
    static QSurvey instance;
    final List<SurveyStateListener> listeners = new ArrayList<>();
    final HashMap<String, Pusher> pusherMap = new HashMap<>();

    private QSurvey() {
        populatePusherMap();
    }

    private void populatePusherMap() {

    }

    public QSurvey appendPusher(Pusher pusher) {
        String supported = pusher.getSupportedGrandType();
        pusherMap.put(supported, pusher);
        return this;
    }

//    public static QSurvey init(Context context) {
//        if (instance == null) {
//            instance = new QSurvey(context);
//            instance.init();
//        }
//        return instance;
//    }

    public static QSurvey getInstance() {
        return instance;
    }


    private void init() {
        QRunner.getInstance().registerRunStateListener(0, this);
    }

    public QSurvey setPersistanceUnit(PersistenceUnit pUnit) {

        return this;
    }

    @Override
    public boolean onRunnerPrepare(String uri) {
        return false;
    }

    @Override
    public boolean onRunnerPrepared(QRunner.PrepareResult result) {
        return false;
    }

    @Override
    public boolean onStartRunning(QPackage qPackage, Test test) {
        return false;
    }

    @Override
    public boolean onRunnerTimeTick(QPackage qPackage, Test test) {
        return false;
    }

    @Override
    public boolean onRunningExerciseChanged(QPackage qPackage, Test test, Exercise exercise, int exerciseIndex) {
        return false;
    }

    @Override
    public boolean onRunningTimeOut(QPackage qPackage, Test test) {
        return false;
    }

    @Override
    public boolean onRunCanceled(QPackage qPackage, Test test) {
        return false;
    }

    @Override
    public boolean onFinishRunning(QPackage qPackage, Test test) {
        try {
            Survey survey = Survey.from(qPackage);
            if (survey == null) {
                //TODO detecter si il sagit d'un Survey Aynchrone ou synchrone et empêcher l'exercise de prendre drat de la fin du Test.
                return false;
            }
            //getPusher(survey).push(test.getCopySheet(), createPushCallback(survey, test.getCopySheet()));
            dispatchSurveyCompleted(survey, test);
        } catch (Survey.InvalidSurveyException e) {
            //Nothing to do, qpackake is not a survey.
        }
        return false;
    }

    private void dispatchSurveyCompleted(Survey survey, Test test) {
        synchronized (listeners) {
            for (SurveyStateListener listener : listeners) {
                listener.onSurveyCompleted(survey, test.getCopySheet());
            }
        }
    }

    //TODO implementer le push callback.
    private Pusher.Callback createPushCallback(Survey survey, CopySheet copySheet) {
        return null;
    }

//    private Pusher getPusher(Survey survey) {
//        return pusherMap.get(survey.auth.getGrandType());
//    }

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

    //TODO doit prevoir une method pour supprimé tous es ordre dja pushé.
    public interface PersistenceUnit {

        void persist(PushOrder order);

        List<PushOrder> findAll();

        void delete(PushOrder order);

    }

    public interface SurveyStateListener {
        void onSurveyCompleted(Survey survey, CopySheet copySheet);
    }

}
