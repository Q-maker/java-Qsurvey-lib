package com.qmaker.survey.core.utils.pushers;

import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.entities.Repository;
import com.qmaker.survey.core.interfaces.PushProcess;
import com.qmaker.survey.core.interfaces.Pusher;

public class JwtPusher implements Pusher {

    @Override
    public PushProcess push(PushOrder order, Callback callback) {
        return null;
    }

    @Override
    public String getSupportedGrandType() {
        return Repository.GRAND_TYPE_JWT;
    }
}
