package com.devup.qcm.survey.engines;

import android.content.Context;

import com.devup.qcm.core.engines.QRunner;
import com.devup.qcm.core.entities.CopySheet;
import com.devup.qcm.core.entities.Exercise;
import com.devup.qcm.core.entities.Test;
import com.devup.qcm.core.io.QPackage;
import com.devup.qcm.survey.entities.Auth;
import com.devup.qcm.survey.entities.PushOrder;
import com.devup.qcm.survey.entities.Survey;

import java.util.List;

public class QSurvey implements QRunner.RunStateListener {
    static QSurvey instance;
    Context context;

    private QSurvey(Context context) {
        this.context = context;
        init();
    }

    public static QSurvey getInstance(Context context) {
        if (instance == null) {
            instance = new QSurvey(context);
            instance.init();
        }
        return instance;
    }


    private void init() {
        QRunner.getInstance().registerRunStateListener(0, this);
    }

    public QSurvey setPersistanceUnit(PersistenceUnit pUnit) {

        return this;
    }

    @Override
    public void onRunnerPrepare(String uri) {

    }

    @Override
    public void onRunnerPrepared(QRunner.PrepareResult result) {

    }

    @Override
    public void onStartRunning(QPackage qPackage, Test test) {

    }

    @Override
    public void onRunnerTimeTick(QPackage qPackage, Test test) {

    }

    @Override
    public void onRunningExerciseChanged(QPackage qPackage, Test test, Exercise exercise, int exerciseIndex) {

    }

    @Override
    public void onRunningTimeOut(QPackage qPackage, Test test) {

    }

    @Override
    public void onRunCanceled(QPackage qPackage, Test test) {

    }

    @Override
    public void onFinishRunning(QPackage qPackage, Test test) {
        try {
            Survey survey = Survey.from(qPackage);
            getPusher(survey).push(test.getCopySheet(), createPushCallback());
        } catch (Survey.InvalidSurveyException e) {
            //Nothing to do, qpackake is not a survey.
        }
    }

    //TODO implementer le push callback.
    private Pusher.Callback createPushCallback() {
        return null;
    }

    private Pusher getPusher(Survey survey) {
        switch (survey.getConfig().auth.getGrandType()) {
            case Auth.GRAND_TYPE_PASSWORD:

                break;
            case Auth.GRAND_TYPE_REFRESH_TOKEN:

                break;

        }
        return null;
    }

    @Override
    public void onResetRunner(QPackage qPackage, Test test) {

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
