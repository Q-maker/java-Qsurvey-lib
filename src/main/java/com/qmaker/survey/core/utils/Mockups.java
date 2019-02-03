package com.qmaker.survey.core.utils;

import com.qmaker.core.engines.Component;
import com.qmaker.core.engines.ComponentManager;
import com.qmaker.core.engines.QSystem;
import com.qmaker.core.engines.Qmaker;
import com.qmaker.core.entities.Qcm;
import com.qmaker.core.io.QPackage;
import com.qmaker.core.io.QProject;
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
            String fileUri = "mockup:///home/istat/Temp/qsurvey-test/";
            MemoryIoInterface ioi = new MemoryIoInterface();
            QSystem system = new QSystem(ioi);
            QPackage project = MockUps.qPackage7(system, fileUri);
            ioi.append(project);
            ComponentManager manager = ComponentManager.getInstance();
            Survey.DefinitionBuilder builder = new Survey.DefinitionBuilder();
            builder//.setType(Survey.TYPE_ANONYMOUS)
                    .setDefaultCompletionMessage(defaultMessage)
                    .setProcessingMessage(processingMessage)
                    .appendRepository(mockUpRepository("0"));
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
            String fileUri = "mockup:///home/istat/Temp/qsurvey-test/";
            MemoryIoInterface ioi = new MemoryIoInterface();
            QSystem system = new QSystem(ioi);
            QPackage project = MockUps.qPackage7(system, fileUri);
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
                    .appendRepository(mockUpRepository("0"));
            Component.Definition def = builder.create();
            manager.apply(def, project);
            return Survey.from(project);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Survey anonymousSurvey() {
        try {
            final String defaultMessage = "Votre feuille de copie a été envoyé avec success";
            final String processingMessage = "Patientez svp...";
            String fileUri = "mockup:///home/istat/Temp/qsurvey-test/";
            MemoryIoInterface ioi = new MemoryIoInterface();
            QSystem system = new QSystem(ioi);
            QPackage project = MockUps.qPackage7(system, fileUri);
            ioi.append(project);
            ComponentManager manager = ComponentManager.getInstance();
            Survey.DefinitionBuilder builder = new Survey.DefinitionBuilder();
            builder.setAnonymous(true)
                    .setDefaultCompletionMessage(defaultMessage)
                    .setProcessingMessage(processingMessage)
                    .appendRepository(mockUpRepository("0"));
            Component.Definition def = builder.create();
            manager.apply(def, project);
            return Survey.from(project);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Repository mockUpRepository() {
        return mockUpRepository(Math.random() + "");
    }

    public static Repository mockUpRepository(String id) {
        Repository.Definition repository = new Repository.Definition();
        repository.setGrandType("firebase");
        repository.setUri("firebase://db/survey/collect/" + id);
        repository.putIdentity("username", "toukea" + id);
        repository.putIdentity("password", "istatyouth" + id);
        return repository.create();
    }
}
