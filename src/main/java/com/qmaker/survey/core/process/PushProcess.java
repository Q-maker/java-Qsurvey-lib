package com.qmaker.survey.core.process;

import com.qmaker.core.entities.CopySheet;
import com.qmaker.survey.core.entities.Survey;
import com.istat.freedev.processor.Process;

public abstract class PushProcess extends Process<PushResult, PushError> {
    @Override
    protected void onExecute(ExecutionVariables executionVariables) throws Exception {
        Survey survey = executionVariables.getVariable(0);
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
