package com.qmaker.survey.core.entities;

import com.qmaker.core.interfaces.IconItem;
import com.qmaker.core.interfaces.JSONable;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class Repository implements JSONable, IconItem {
    public final static String GRAND_TYPE_WSSE = "wsse", GRAND_TYPE_JWT = "jwt";
    public final static String GRAND_TYPE_FTP = "ftp";
    public final static String GRAND_TYPE_HTTP_BASIC = "http_basic";
    public final static String GRAND_TYPE_REFRESH_TOKEN = "refresh_token";
    public final static String IDENTITY_USER_NAME = "username";
    public final static String IDENTITY_PASSWORD = "password";
    public final static String IDENTITY_TOKEN_ID = "id";
    public static final String GRAND_TYPE_HTTP_DIGEST = "http_digest";
    String uri;
    String grandType;
    Form identityForm;

    public Form.Field putIdentity(String name, String value) {
        if (identityForm != null) {
            return identityForm.put(name, value);
        }
        return null;
    }

    Repository() {

    }

    public String getIdentity(String name) {
        Form.Field field = identityForm.getField(name);
        if (field == null) {
            return null;
        }
        return field.getValueString("");
    }

    public HashMap<String, Object> getIdentityMap() {
//        HashMap<String, String> result = new HashMap();
//        for (Form.Field field : identityForm.getFields()) {
//            result.put(field.getName(), field.getValueString());
//        }
//        return result;
        return getIdentities().getContentMap();
    }

    public Form getIdentities() {
        return identityForm;
    }

    public String getGrandType() {
        return grandType;
    }

    public String getUri() {
        List<Form.Field> fields = identityForm.getFields();
        String out = uri;
        for (Form.Field field : fields) {
            out = uri.replace("{" + field.getName() + "}", field.getValueString());
        }
        return out;
    }

    /*
               {
                "grant_type": "password",
                "identity": {
                 "user": {
                  "username": "test2018",
                  "password": "admin@2018"
                 }
                }
               }
               ---------------------------------
               {
                "grant_type": "refresh_token",
                "identity": {
                 "token": {
                  "id": "d_xQXP81d0L4a9mPbo8xAixAJy4Baj-KrLuV1hhlGpDC1yYXJG_9G5Olarz9Yopb"
                 }
                }
               }
                */
    final static String FIELD_GRAND_TYPE = "grant_type";
    final static String FIELD_IDENTITY = "identity";
    final static String FIELD_IDENTITY_TOKEN = "token";
    final static String FIELD_IDENTITY_USER = "user";

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public JSONObject toJson() {
        try {
            return new JSONObject(toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    //    @Override
//    public JSONObject toJson() {
//        try {
//            JSONObject json = new JSONObject();
//            json.put(FIELD_GRAND_TYPE, grandType);
//            JSONObject jsonIdentity = new JSONObject();
//            Gson gson = new Gson();
//            JSONObject jsonIdentityContent = new JSONObject(gson.toJson(getIdentities()));
//            jsonIdentity.put(grandType.equals(GRAND_TYPE_HTTP_BASIC) ? FIELD_IDENTITY_USER : FIELD_IDENTITY_TOKEN, jsonIdentityContent);
//            json.put(FIELD_IDENTITY, jsonIdentity);
//            return json;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new JSONObject();
//        }
//    }

//    public static Repository fromHttpBasic(String username, String password) {
//        Repository auth = new Repository();
//        auth.grandType = GRAND_TYPE_HTTP_BASIC;
//        auth.putIdentity(Repository.IDENTITY_USER_NAME, username);
//        auth.putIdentity(Repository.IDENTITY_PASSWORD, password);
//        return auth;
//    }
//
//    public static Repository fromWsse(String username, String password) {
//        Repository auth = new Repository();
//        auth.grandType = GRAND_TYPE_WSSE;
//        auth.putIdentity(Repository.IDENTITY_USER_NAME, username);
//        auth.putIdentity(Repository.IDENTITY_PASSWORD, password);
//        return auth;
//    }
//
//    public static Repository fromFtp(String username, String password) {
//        Repository auth = new Repository();
//        auth.grandType = GRAND_TYPE_FTP;
//        auth.putIdentity(Repository.IDENTITY_USER_NAME, username);
//        auth.putIdentity(Repository.IDENTITY_PASSWORD, password);
//        return auth;
//    }
//
//    public static Repository fromRefreshToken(String tokenId) {
//        Repository auth = new Repository();
//        auth.grandType = GRAND_TYPE_REFRESH_TOKEN;
//        auth.putIdentity(Repository.IDENTITY_TOKEN_ID, tokenId);
//        return auth;
//    }

    String iconUri, name, description;

    @Override
    public String getIconUri() {
        return iconUri;
    }

    @Override
    public CharSequence getTitle() {
        return name + ":" + uri + ":" + grandType;
    }

    public String getName() {
        return name;
    }

    @Override
    public CharSequence getDescription() {
        return description;
    }

    public static class Definition {

        String name, description, iconUri, uri, grandType;
        Form.Definition identityFormDefinition = new Form.Definition();

        public Definition setDescription(String description) {
            this.description = description;
            return this;
        }

        public Definition setName(String name) {
            this.name = name;
            return this;
        }

        public Definition setIconUri(String iconUri) {
            this.iconUri = iconUri;
            return this;
        }

        public Definition setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Definition setGrandType(String grandType) {
            this.grandType = grandType;
            return this;
        }

        public HashMap<String, Form.Field> setIdentity(HashMap<String, String> identity) {
            this.identityFormDefinition.clear();
            if (identity != null) {
                return this.identityFormDefinition.putAll(identity);
            }
            return null;
        }

        public Definition setIdentity(Form form) {
            this.identityFormDefinition = new Form.Definition(form);
            return this;
        }

        public Form.Field putIdentity(String name, String value) {
            return this.identityFormDefinition.put(value, name);
        }

        public Form.Field putIdentity(String name, String label, String inputType) {
            return this.identityFormDefinition.put(name, label, inputType);
        }

        public Form.Field putIdentity(String name, String label, String inputType, Object value) {
            return this.identityFormDefinition.put(name, label, inputType, value);
        }

        public Form.Definition getIdentityFormDefinition() {
            return identityFormDefinition;
        }

        public Repository create() {
            Repository repository = new Repository();
            repository.name = name;
            repository.grandType = grandType;
            repository.uri = uri;
            repository.iconUri = iconUri;
            repository.identityForm = identityFormDefinition.create();
            repository.description = description;
            return repository;
        }

        public void putIdentity(Form form) {
            this.identityFormDefinition = new Form.Definition(form);
        }

        public void putIdentity(Form.Definition definition) {
            this.identityFormDefinition = new Form.Definition(definition);
        }
    }
}