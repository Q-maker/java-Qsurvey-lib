package com.qmaker.survey.core.interfaces;

import com.qmaker.survey.core.entities.PushOrder;

public interface Pusher {

    PushProcess push(PushOrder order, Pusher.Callback callback);

    interface Callback {

    }
}
