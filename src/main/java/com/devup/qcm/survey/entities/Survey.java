package com.devup.qcm.survey.entities;

import com.devup.qcm.core.engines.Component;
import com.devup.qcm.core.engines.ComponentManager;
import com.devup.qcm.core.io.QPackage;
import com.devup.qcm.core.utils.Bundle;
import com.devup.qcm.core.utils.QFileUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import istat.android.base.tools.ToolKits;

public class Survey {
    final static String TAG = "survey";
    Component component;

    private Survey(Component component) {
        this.component = component;
    }

    public final static Survey from(QPackage qPackage) throws InvalidSurveyException {
        Component component = ComponentManager.getInstance().fetch(qPackage).getComponent(TAG);
        if (component == null) {
            return null;
        }
        return new Survey(component);
    }

    public QPackage getQpackage() {
        return component.getQPackage();
    }

    public final static String TYPE_ANONYMOUS = "anonymous";
    public Auth auth;
    public String destinationUri;
    public String message;
    public String type = TYPE_ANONYMOUS;
    Bundle extras;

    public Bundle getExtras() {
        return extras;
    }

    public URI getDestinationUri() {
        return QFileUtils.createURI(destinationUri);
    }

    public static class InvalidSurveyException extends Exception {
        public InvalidSurveyException(Throwable e) {
            super(e);
        }

        public InvalidSurveyException(String message) {
            super(message);
        }
    }

}
