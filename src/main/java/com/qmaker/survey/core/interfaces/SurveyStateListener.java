package com.qmaker.survey.core.interfaces;

import com.qmaker.survey.core.entities.Survey;
import com.qmaker.survey.core.utils.PayLoad;

public interface SurveyStateListener {
    int STATE_PREPARED = 0x00000001,
            STATE_STARTED = 0x00000010,
            STATE_PAUSED = 0x00000011,
            STATE_RESUME = 0x00000101,
            STATE_TIME_TICK = 0x00000100,
            STATE_EXERCISE_CHANGED = 0x00001000,
            STATE_TIME_OUT = 0x00010000,
            STATE_CANCELED = 0x00100000,
            STATE_RESET = 0x01000000,
            STATE_COMPLETED = 0x10000000,
            STATE_FINISH = STATE_COMPLETED | STATE_CANCELED | STATE_TIME_OUT;

    void onSurveyStateChanged(int state, Survey survey, PayLoad payLoad);
}
