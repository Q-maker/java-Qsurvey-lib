package com.qmaker.survey.core.utils;

import com.qmaker.core.engines.Component;
import com.qmaker.core.engines.ComponentManager;
import com.qmaker.core.engines.QSystem;
import com.qmaker.core.entities.Author;
import com.qmaker.core.entities.Qcm;
import com.qmaker.core.io.QPackage;
import com.qmaker.core.utils.MemoryIoInterface;
import com.qmaker.core.utils.MockUps;
import com.qmaker.survey.core.entities.Repository;
import com.qmaker.survey.core.entities.Survey;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Mockups {

    public static Survey simpleSurvey() {
        try {
            final String defaultMessage = "Votre feuille de copie a été envoyé avec success";
            final String processingMessage = "Patientez svp...";
            String fileUri = "mockup:///home/istat/Temp/qsurvey-test-async/";
            MemoryIoInterface ioi = new MemoryIoInterface();
            QSystem system = new QSystem(ioi);
            QPackage project = MockUps.qPackage7(system, fileUri);
            project.getQuestionnaire().setId("survey-demo-asynchron");
            project.getQuestionnaire().setTitle("Survey-demo-asynchron");
            project.getQuestionnaire().setAuthor(author());
            ioi.append(project);
            ComponentManager manager = ComponentManager.getInstance();
            Survey.DefinitionBuilder builder = new Survey.DefinitionBuilder();
            builder//.setType(Survey.TYPE_ANONYMOUS)
                    .setDefaultCompletionMessage(defaultMessage)
                    .setProcessingMessage(processingMessage)
                    .appendRepository(mockUpFirebaseDbRepository("simple_push_campaign"));
            Component.Definition def = builder.create();
            manager.apply(def, project);
            return Survey.from(project);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Survey blockingSurvey() {
        try {
            final String defaultMessage = "Votre feuille de copie a été envoyé avec success";
            final String processingMessage = "Patientez svp...";
            String fileUri = "mockup:///home/istat/Temp/qsurvey-test-blocking/";
            MemoryIoInterface ioi = new MemoryIoInterface();
            QSystem system = new QSystem(ioi);
            QPackage project = MockUps.qPackage7(system, fileUri);
            project.getQuestionnaire().setId("survey-demo-bloking");
            project.getQuestionnaire().setTitle("Survey-demo-bloking");
            project.getQuestionnaire().setAuthor(author());
            project.getSummary().getSubject().setDescription("");
            List<Qcm> qcms = project.getQuestionnaire().getQcms();
            Random random = new Random();
            for (Qcm qcm : qcms) {
                if (random.nextBoolean()) {
                    qcm.getQuestion().setSoundUri("file:///sdcard/audioTest.3gpp");
                }
            }
            ioi.append(project);
            ComponentManager manager = ComponentManager.getInstance();
            Survey.DefinitionBuilder builder = new Survey.DefinitionBuilder();
            builder.setBlockingPublisherAllowed(true)
                    .setDefaultCompletionMessage(defaultMessage)
                    .setProcessingMessage(processingMessage)
                    .appendRepository(mockUpFirebaseDbRepository("blocking_push_campaign"));
            Component.Definition def = builder.create();
            manager.apply(def, project);
            return Survey.from(project);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Survey applyBlockingSurvey(QPackage qPackage, String surveyId) throws IOException, Survey.InvalidSurveyException {
        ComponentManager componentManager = ComponentManager.getInstance();
        componentManager.apply(getBlockingSurveyComponentDefinition(surveyId), qPackage);
        Survey survey = Survey.from(qPackage);
        return survey;
    }

    public static Component.Definition getBlockingSurveyComponentDefinition(String id) {
        final String defaultMessage = "CopySheet sent successfully.";
        final String processingMessage = "Please Wait...";
        Survey.DefinitionBuilder builder = new Survey.DefinitionBuilder();
        builder.setBlockingPublisherAllowed(true)
                .setDefaultCompletionMessage(defaultMessage)
                .setProcessingMessage(processingMessage)
                .appendRepository(mockUpFirebaseDbRepository(id));
        Component.Definition def = builder.create();
        return def;
    }

    public static Survey anonymousSurvey() {
        try {
            final String defaultMessage = "Votre feuille de copie a été envoyé avec success";
            final String processingMessage = "Patientez svp...";
            String fileUri = "mockup:///home/istat/Temp/qsurvey-test-anonymous/";
            MemoryIoInterface ioi = new MemoryIoInterface();
            QSystem system = new QSystem(ioi);
            QPackage project = MockUps.qPackage7(system, fileUri);
            project.getQuestionnaire().setId("survey-demo-anonymous");
            project.getQuestionnaire().setTitle("Survey-demo-anonymous");
            project.getQuestionnaire().setAuthor(author());
            ioi.append(project);
            ComponentManager manager = ComponentManager.getInstance();
            Survey.DefinitionBuilder builder = new Survey.DefinitionBuilder();
            builder.setAnonymous(true)
                    .setDefaultCompletionMessage(defaultMessage)
                    .setProcessingMessage(processingMessage)
                    .appendRepository(mockUpFirebaseDbRepository("anonymous_push_campaign"));
            Component.Definition def = builder.create();
            manager.apply(def, project);
            return Survey.from(project);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Survey anonymousSurvey2() {
        try {
            final String defaultMessage = "Votre feuille de copie a été envoyé avec success";
            final String processingMessage = "Patientez svp...";
            String fileUri = "mockup:///home/istat/Temp/qsurvey-test-anonymous2/";
            MemoryIoInterface ioi = new MemoryIoInterface();
            QSystem system = new QSystem(ioi);
            QPackage project = MockUps.qPackage7(system, fileUri);
            project.getQuestionnaire().setId("survey-demo-anonymous2");
            project.getQuestionnaire().setTitle("Survey-demo-anonymous2");
            project.getQuestionnaire().setAuthor(author());
            ioi.append(project);
            ComponentManager manager = ComponentManager.getInstance();
            Survey.DefinitionBuilder builder = new Survey.DefinitionBuilder();
            builder.setAnonymous(true)
                    .setDefaultCompletionMessage(defaultMessage)
                    .setProcessingMessage(processingMessage)
                    .appendRepository(mockUpFirebaseDbRepository("anonymous2_push_campaign_0"))
                    .appendRepository(mockUpFirebaseDbRepository("anonymous2_push_campaign_1"));
            Component.Definition def = builder.create();
            manager.apply(def, project);
            return Survey.from(project);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Repository mockUpFirebaseDbRepository() {
        return mockUpFirebaseDbRepository(Math.random() + "");
    }

    public static Repository mockUpRepository(String id, String grandType) {
        Repository.Definition repository = new Repository.Definition();
        repository.setName("QuizBucket");
        repository.setGrandType(grandType);
        repository.setUri("firebase://db/survey/collect/" + id);
        repository.putIdentity("username", "toukea" + id);
        repository.putIdentity("password", "istatyouth" + id);
        return repository.create();
    }

    public static Repository mockUpFirebaseDbRepository(String id) {
        return mockUpRepository(id, "firebase");
    }

    private static Author author() {
        Author author = new Author("id0", "Toukea Tatsi", "Jephté");
        author.bibliography = "Ceci est la bibliographie de " + author.getDisplayName() + " qui explique qui il est.et ce qu'il fait.";
        author.photoUri = "https://unsplash.it/200/200/?random&id_toukeatatsij";
        return author;
    }
}
