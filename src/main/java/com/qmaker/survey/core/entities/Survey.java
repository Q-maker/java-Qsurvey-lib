package com.qmaker.survey.core.entities;

import com.google.gson.reflect.TypeToken;
import com.qmaker.core.engines.Component;
import com.qmaker.core.engines.ComponentManager;
import com.qmaker.core.entities.Author;
import com.qmaker.core.entities.CopySheet;
import com.qmaker.core.entities.QSummary;
import com.qmaker.core.entities.Questionnaire;
import com.qmaker.core.entities.Test;
import com.qmaker.core.io.QPackage;
import com.qmaker.core.utils.ToolKits;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import istat.android.base.tools.TextUtils;

//Les destination doivent être une liste. afin qu'une survey puisse être envoyé vers plusieurs zone.
public class Survey {

    public final static String TYPE_ANONYMOUS = "anonymous",
            TYPE_ASYNCHONOUS = "asynchronous",
            TYPE_SYNCHRONOUS = "synchronous";
    final static String NAMESPACE = "survey";
    Component component;
    public static String
            FIELD_ID = "id",
            FIELD_TYPE = "type",
            FIELD_REPOSITORIES = "repositories",
            FIELD_DEFAULT_COMPLETION_MESSAGE = "default_completion_message",
            FIELD_PROCESSING_MESSAGE = "processing_message";

    private Survey(Component component) {
        this.component = component;
    }

    public final static Survey from(QPackage qPackage) throws InvalidSurveyException {
        Component component = ComponentManager.getInstance().fetch(qPackage).getComponent(NAMESPACE);
        if (component == null) {
            throw new InvalidSurveyException(qPackage);
        }
        return new Survey(component);
    }

    public QPackage getQPackage() {
        return component.getQPackage();
    }

    public QSummary getQuestionnaireSummary() {
        return getQPackage().getSummary();
    }

    public QSummary.Config getQuestionnaireConfig() {
        return getQPackage().getSummary().getConfig();
    }

    public Questionnaire getQuestionaire() throws IOException {
        return getQPackage().getQuestionnaire();
    }

    List<Repository> repositories;

    public List<Repository> getRepositories() {
        if (repositories != null) {
            return repositories;
        }
        Type listType = new TypeToken<ArrayList<Repository>>() {
        }.getType();
        List<Repository> repositories = component.getSummaryPropertyArray(FIELD_REPOSITORIES, listType);
        this.repositories = Collections.unmodifiableList(repositories);
        return repositories;
    }

    public String getType() {
        return component.getSummaryStringProperty(FIELD_TYPE);
    }

    public String getId() {
        try {
            String id = component.getSummaryStringProperty(FIELD_ID);
            if (TextUtils.isEmpty(id)) {
                id = component.getQPackage().getSummary().getId();
            }
            return "[" + ToolKits.generateID() + "]" + id;
        } catch (Exception e) {
            return null;
        }
    }

    public Repository getRepository(int index) {
        List<Repository> repositories = getRepositories();
        return repositories != null && repositories.size() < index ? repositories.get(index) : null;
    }

    public String getProcessingMessage() {
        return component.getSummaryStringProperty(FIELD_PROCESSING_MESSAGE);
    }

    public String getDefaultCompletionMessage() {
        return component.getSummaryStringProperty(FIELD_DEFAULT_COMPLETION_MESSAGE);
    }

    public static class InvalidSurveyException extends Exception {
        public InvalidSurveyException(Throwable e) {
            super(e);
        }

        public InvalidSurveyException(String message) {
            super(message);
        }

        public InvalidSurveyException(QPackage qPackage) {
            super("This qpackage doesn't content any Survey component." + (qPackage != null ? " uri" + qPackage.getUriString() : ""));
        }
    }

    public static class DefinitionBuilder {
        String type, id;
        final List<Repository> repositories = new ArrayList<>();
        String processingMassage, defaultCompletionMessage;


        public DefinitionBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public DefinitionBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public DefinitionBuilder setDefaultCompletionMessage(String defaultCompletionMessage) {
            this.defaultCompletionMessage = defaultCompletionMessage;
            return this;
        }

        public DefinitionBuilder setProcessingMessage(String processingMassage) {
            this.processingMassage = processingMassage;
            return this;
        }

        public DefinitionBuilder appendRepository(Repository repository) {
            if (!this.repositories.contains(repository)) {
                this.repositories.add(repository);
            }
            return this;
        }

        public DefinitionBuilder appendRepositores(Repository... repositories) {
            for (Repository repository : repositories) {
                appendRepository(repository);
            }
            return this;
        }

        public DefinitionBuilder appendRepositores(List<Repository> repositories) {
            for (Repository repository : repositories) {
                appendRepository(repository);
            }
            return this;
        }

        public Component.Definition create() {
            Component.Definition definition = new Component.Definition(Survey.NAMESPACE);
            definition.setSummaryProperties(FIELD_ID, id);
            definition.setSummaryProperties(FIELD_TYPE, type);
            definition.setSummaryProperties(FIELD_DEFAULT_COMPLETION_MESSAGE, defaultCompletionMessage);
            definition.setSummaryProperties(FIELD_PROCESSING_MESSAGE, processingMassage);
            definition.setSummaryProperties(FIELD_REPOSITORIES, repositories);
            return definition;
        }
    }

    public Result getResult(Test test) {
        if (test == null) {
            return null;
        }
        return new Result(test);
    }

    public class Result {
        final Test test;
        final int state;
        CopySheet copySheet;

        public Result(Test test) {
            this.test = test;
            this.state = test.state;
        }

        public CopySheet getCopySheet() {
            if (copySheet == null) {
                copySheet = test.getCopySheet();
            }
            return copySheet;
        }

        public int getState() {
            return state;
        }

        public Test getTest() {
            return Test.copy(test);
        }

        public Survey getOrigin() {
            return Survey.this;
        }
    }

}
