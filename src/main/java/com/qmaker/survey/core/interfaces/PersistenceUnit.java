package com.qmaker.survey.core.interfaces;

import com.qmaker.survey.core.entities.PushOrder;

import java.util.List;

//TODO doit prevoir une method pour supprimé tous es ordre dja pushé.
public interface PersistenceUnit {

    boolean persist(PushOrder order);

    List<PushOrder> findAll();

    boolean delete(PushOrder order);

}
