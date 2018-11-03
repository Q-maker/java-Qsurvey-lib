package com.qmaker.survey.core.entities;

import com.qmaker.core.engines.Component;
import com.qmaker.core.engines.ComponentManager;
import com.qmaker.core.io.QPackage;

import java.util.Collections;
import java.util.List;

//Les destination doivent être une liste. afin qu'une survey puisse être envoyé vers plusieurs zone.
public class Survey {
    final static String TAG = "survey";
    Component component;
    public static String FIELD_TYPE = "type",
            FIELD_AUTH_LIST = "auth",
            FIELD_DEFAULT_MESSAGE = "message";

    private Survey(Component component) {
        this.component = component;
    }

    public final static Survey from(QPackage qPackage) throws InvalidSurveyException {
        Component component = ComponentManager.getInstance().fetch(qPackage).getComponent(TAG);
        if (component == null) {
            throw new InvalidSurveyException();
        }
        return new Survey(component);
    }

    public QPackage getQPackage() {
        return component.getQPackage();
    }

    public final static String TYPE_ANONYMOUS = "anonymous";
    List<Auth> authList;

    public List<Auth> getAuthList() throws IllegalAccessException, InstantiationException {
        if (authList != null) {
            return authList;
        }
        authList = Collections.unmodifiableList(component.getSummaryProperties(FIELD_AUTH_LIST, List.class));
        return authList;
    }

    public String getDefaultMessage() {
        return component.getSummaryStringProperty(FIELD_DEFAULT_MESSAGE);
    }

    public static class InvalidSurveyException extends Exception {
        public InvalidSurveyException(Throwable e) {
            super(e);
        }

        public InvalidSurveyException(String message) {
            super(message);
        }

        public InvalidSurveyException() {
            super("This qpackage doesn't content any Survey component.");
        }
    }

}
