package com.qmaker.survey.core.utils;

import com.qmaker.survey.core.entities.PushOrder;
import com.qmaker.survey.core.interfaces.PersistenceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoryPersistenceUnit implements PersistenceUnit {
    final HashMap<String, PushOrder> pushOrderHashMap = new HashMap<>();

    @Override
    public boolean persist(PushOrder order) {
        pushOrderHashMap.put(order.getId(), order);
        return true;
    }

    @Override
    public List<PushOrder> findAll() {
        return new ArrayList(pushOrderHashMap.values());
    }

    @Override
    public boolean delete(PushOrder order) {
        return pushOrderHashMap.remove(order.getId()) != null;
    }
}
