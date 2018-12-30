package com.devup.qcm.survey;

import com.qmaker.core.engines.Component;
import com.qmaker.core.engines.ComponentManager;
import com.qmaker.core.engines.QSystem;
import com.qmaker.core.engines.Qmaker;
import com.qmaker.core.entities.Author;
import com.qmaker.core.entities.Qcm;
import com.qmaker.core.entities.Questionnaire;
import com.qmaker.core.io.QPackage;
import com.qmaker.core.io.QProject;
import com.qmaker.core.utils.MockUps;
import com.qmaker.core.utils.ZipFileIoInterface;
import com.qmaker.survey.core.entities.Repository;
import com.qmaker.survey.core.entities.Survey;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void buildQSurveyTest() throws Exception {
        String uri = "file:///home/istat/Temp/qsurvey/";
        QProject project = Qmaker.exist(uri) ? Qmaker.edit(uri) : Qmaker.newProject(uri);
        project.setQuestionnaire(MockUps.questionnaireAllType());
        project.getConfig().setRandomEnable(false);
        File backgroundAudioFile = new File("/home/istat/Temp/audio_qcm.mp3");
        File audioPlayFile = new File("/home/istat/Temp/audio_qcm.m4a");
        if (!backgroundAudioFile.exists()) {
            assertTrue(false);
            return;
        }
        project.getResEditor().set(QPackage.Resource.TYPE_SOUNDS, backgroundAudioFile);
        String entryPath = project.getQcmResEditor(0).getQuestionResEditor().set(QPackage.Resource.TYPE_SOUNDS, audioPlayFile);
        for (Qcm qcm : project.getQcmList()) {
            qcm.getUriMap().setSoundUri(entryPath);
        }
        ZipFileIoInterface zipIoInterface = new ZipFileIoInterface();
        QSystem zipSystem = new QSystem(zipIoInterface);
        QPackage repackaged = zipSystem.repack(project);
        zipSystem.save(repackaged, "file:///home/istat/Temp/qsurvey.qcm");
    }

    @Test
    public void testApplySurveyComponentAndSave() throws Exception {
        final String defaultMessage = "Votre feuille de copie a été envoyé avec success";
        final String processingMessage = "Patientez svp...";
        String fileUri = "file:///home/istat/Temp/qsurvey-test/";
        QProject project = Qmaker.edit(fileUri);
        project.setQuestionnaire(MockUps.questionnaireAllType());
        ComponentManager manager = ComponentManager.getInstance();
        Survey.DefinitionBuilder builder = new Survey.DefinitionBuilder();
        builder//.setType(Survey.TYPE_ANONYMOUS)
                .setDefaultCompletionMessage(defaultMessage)
                .setProcessingMessage(processingMessage)
                .appendRepository(mockUpRepository("0"))
                .appendRepository(mockUpRepository("1"));
        Component.Definition def = builder.create();
        manager.apply(def, project);
        project.save();
        project = Qmaker.edit(fileUri);
        Survey survey = Survey.from(project);
        assertEquals(survey.getDefaultCompletionMessage(), defaultMessage);
        assertEquals(survey.getProcessingMessage(), processingMessage);
        List<Repository> repositories = survey.getRepositories();
        assertTrue(!repositories.isEmpty());
        buildProjectTest(project);
    }

    @Test
    public void readRepositoryFromSurveyTest() throws Exception {
        String fileUri = "file:///home/istat/Temp/qsurvey-test/";
        QProject project = Qmaker.edit(fileUri);
        Survey survey = Survey.from(project);
        List<Repository> repositories = survey.getRepositories();
        assertTrue(!repositories.isEmpty());
    }

    private Repository mockUpRepository() {
        return mockUpRepository("");
    }

    private Repository mockUpRepository(String id) {
        Repository.Definition repository = new Repository.Definition();
        repository.setGrandType(Repository.GRAND_TYPE_WSSE);
        repository.setUri("www.google.com/" + id);
        repository.putIdentity("username", "toukea" + id);
        repository.putIdentity("password", "istatyouth" + id);
        return repository.create();
    }

    public void buildProjectTest(QPackage project) throws Exception {
        ZipFileIoInterface zipIoInterface = new ZipFileIoInterface();
        QSystem zipSystem = new QSystem(zipIoInterface);
        QPackage repackaged = zipSystem.repack(project);
        zipSystem.save(repackaged, "file:///home/istat/Temp/qsurvey-test.qcm");
    }
}