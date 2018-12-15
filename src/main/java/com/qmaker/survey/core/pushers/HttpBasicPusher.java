package com.qmaker.survey.core.pushers;

import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.entities.Repository;
import com.qmaker.survey.core.interfaces.PushProcess;
import com.qmaker.survey.core.interfaces.Pusher;

public class HttpBasicPusher implements Pusher {
    @Override
    public PushProcess push(PushOrder order, Callback callback) throws Exception {
        return null;
    }

    @Override
    public String getSupportedGrandType() {
        return Repository.GRAND_TYPE_HTTP_BASIC;
    }
}
