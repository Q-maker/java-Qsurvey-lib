package com.qmaker.survey.core.utils.pushers;

import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.interfaces.PushProcess;
import com.qmaker.survey.core.interfaces.Pusher;

public class MemoryPusher implements Pusher {
    public final static String ACCEPTED_GRAND_TYPE = "memory";

    @Override
    public PushProcess push(PushOrder order, Callback callback) {
        return null;
    }

    @Override
    public String getSupportedGrandType() {
        return ACCEPTED_GRAND_TYPE;
    }
}
