package com.devup.qcm.survey.process;

import com.devup.qcm.core.entities.CopySheet;
import com.devup.qcm.survey.entities.Survey;
import com.istat.freedev.processor.Process;

public abstract class PushProcess extends Process<PushResult, PushError> {
    @Override
    protected void onExecute(ExecutionVariables executionVariables) throws Exception {
        Survey.Config config = executionVariables.getVariable(0);
        CopySheet copySheet = executionVariables.getVariable(1);

    }

    @Override
    protected void onResume() {

    }

    @Override
    protected void onPaused() {

    }

    @Override
    protected void onStopped() {

    }

    @Override
    protected void onCancel() {

    }

    @Override
    public boolean isPaused() {
        return false;
    }
}
