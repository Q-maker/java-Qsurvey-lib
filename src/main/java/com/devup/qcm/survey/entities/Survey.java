package com.devup.qcm.survey.entities;

import com.devup.qcm.core.io.QPackage;
import com.devup.qcm.core.utils.QFileUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import istat.android.base.tools.ToolKits;

public class Survey {
    final static String RES_SECTION = "x-survey";
    final static String RES_CONFIG_NAME = "config";
    Config config;
    QPackage qPackage;

    private Survey() {

    }

    public final static Survey from(QPackage qPackage) throws IllegalArgumentException {
        Survey survey = new Survey();
        survey.qPackage = qPackage;
        try {
            Gson gson = new Gson();
            InputStream configStream = survey.qPackage.getResource().getEntry(RES_SECTION, RES_CONFIG_NAME).openInputStream();
            String content = ToolKits.Stream.streamToString(configStream);
            survey.config = gson.fromJson(content, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return survey;
    }

    public Config getConfig() {
        return config;
    }

    public static class Config {
        public final static String TYPE_ANONYMOUS = "anonymous";
        public Auth auth;
        public String destinationUri;
        public String message;
        public String type = TYPE_ANONYMOUS;

        public URI getDestinationUri() {
            return QFileUtils.createURI(destinationUri);
        }
    }

}
