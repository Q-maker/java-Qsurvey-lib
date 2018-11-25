package com.qmaker.survey.core.entities;

import com.qmaker.core.engines.Component;
import com.qmaker.core.engines.ComponentManager;
import com.qmaker.core.io.QPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Les destination doivent être une liste. afin qu'une survey puisse être envoyé vers plusieurs zone.
public class Survey {

    public final static String TYPE_ANONYMOUS = "anonymous",
            TYPE_ASYNCHONOUS = "asynchronous",
            TYPE_SYNCHRONOUS = "synchronous";
    final static String NAMESPACE = "survey";
    Component component;
    public static String FIELD_TYPE = "type",
            FIELD_REPOSITORIES = "repositories",
            FIELD_DEFAULT_COMPLETION_MESSAGE = "default_completion_message",
            FIELD_PROCESSING_MESSAGE = "processing_message";

    private Survey(Component component) {
        this.component = component;
    }

    public final static Survey from(QPackage qPackage) throws InvalidSurveyException {
        Component component = ComponentManager.getInstance().fetch(qPackage).getComponent(NAMESPACE);
        if (component == null) {
            throw new InvalidSurveyException();
        }
        return new Survey(component);
    }

    public QPackage getQPackage() {
        return component.getQPackage();
    }

    List<Repository> repositories;

    public List<Repository> getRepositories() throws IllegalAccessException, InstantiationException {
        if (repositories != null) {
            return repositories;
        }
        repositories = Collections.unmodifiableList(component.getSummaryProperties(FIELD_REPOSITORIES, List.class));
        return repositories;
    }

    public String getType() {
        return component.getSummaryStringProperty(FIELD_TYPE);
    }

    public Repository getRepository(int index) {
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

        public InvalidSurveyException() {
            super("This qpackage doesn't content any Survey component.");
        }
    }

    public static class DefinitionBuilder {
        String type;
        final List<Repository> repositories = new ArrayList<>();
        String processingMassage, defaultCompletionMessage;

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
            definition.setSummaryProperties(FIELD_TYPE, type);
            definition.setSummaryProperties(FIELD_DEFAULT_COMPLETION_MESSAGE, defaultCompletionMessage);
            definition.setSummaryProperties(FIELD_PROCESSING_MESSAGE, processingMassage);
            definition.setSummaryProperties(FIELD_REPOSITORIES, repositories);
            return definition;
        }
    }

}
