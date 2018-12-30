package com.qmaker.survey.core.entities;

import com.qmaker.core.interfaces.IconItem;
import com.qmaker.core.interfaces.JSONable;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

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
    HashMap<String, String> identity = new HashMap<>();

    public Repository putIdentity(String name, String value) {
        identity.put(name, value);
        return this;
    }

    Repository() {

    }

    public String getIdentity(String name) {
        return identity.get(name);
    }

    public String getGrandType() {
        return grandType;
    }

    public String getUri() {
        return uri;
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
        return toJson().toString();
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put(FIELD_GRAND_TYPE, grandType);
            JSONObject jsonIdentity = new JSONObject();
            Gson gson = new Gson();
            JSONObject jsonIdentityContent = new JSONObject(gson.toJson(identity));
            jsonIdentity.put(grandType.equals(GRAND_TYPE_HTTP_BASIC) ? FIELD_IDENTITY_USER : FIELD_IDENTITY_TOKEN, jsonIdentityContent);
            json.put(FIELD_IDENTITY, jsonIdentity);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static Repository fromHttpBasic(String username, String password) {
        Repository auth = new Repository();
        auth.grandType = GRAND_TYPE_HTTP_BASIC;
        auth.putIdentity(Repository.IDENTITY_USER_NAME, username);
        auth.putIdentity(Repository.IDENTITY_PASSWORD, password);
        return auth;
    }

    public static Repository fromWsse(String username, String password) {
        Repository auth = new Repository();
        auth.grandType = GRAND_TYPE_WSSE;
        auth.putIdentity(Repository.IDENTITY_USER_NAME, username);
        auth.putIdentity(Repository.IDENTITY_PASSWORD, password);
        return auth;
    }

    public static Repository fromFtp(String username, String password) {
        Repository auth = new Repository();
        auth.grandType = GRAND_TYPE_FTP;
        auth.putIdentity(Repository.IDENTITY_USER_NAME, username);
        auth.putIdentity(Repository.IDENTITY_PASSWORD, password);
        return auth;
    }

    public static Repository fromRefreshToken(String tokenId) {
        Repository auth = new Repository();
        auth.grandType = GRAND_TYPE_REFRESH_TOKEN;
        auth.putIdentity(Repository.IDENTITY_TOKEN_ID, tokenId);
        return auth;
    }

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
        HashMap<String, String> identity = new HashMap<>();

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

        public Definition setIdentity(HashMap<String, String> identity) {
            this.identity.clear();
            if (identity != null) {
                this.identity.putAll(identity);
            }
            return this;
        }

        public Definition putIdentity(String name, String value) {
            this.identity.put(name, value);
            return this;
        }

        public Repository create() {
            Repository repository = new Repository();
            repository.name = name;
            repository.grandType = grandType;
            repository.uri = uri;
            repository.iconUri = iconUri;
            repository.identity = identity;
            repository.description = description;
            return repository;
        }
    }
}