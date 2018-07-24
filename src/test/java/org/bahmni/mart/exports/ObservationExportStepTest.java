package org.bahmni.mart.exports;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.ObsIncrementalUpdateStrategy;
import org.bahmni.mart.exports.writer.DatabaseObsWriter;
import org.bahmni.mart.form.ObservationProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.ObjectFactory;

import javax.sql.DataSource;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@PrepareForTest({BatchUtils.class, JobDefinitionUtil.class})
@RunWith(PowerMockRunner.class)
public class ObservationExportStepTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private FreeMarkerEvaluator<BahmniForm> freeMarkerEvaluator;

    @Mock
    private ObjectFactory<ObservationProcessor> observationProcessorFactory;

    @Mock
    private ObjectFactory<DatabaseObsWriter> obsWriterObjectFactory;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private ObsIncrementalUpdateStrategy obsIncrementalUpdater;

    private ObservationExportStep observationExportStep;
    private static final String JOB_NAME = "job Name";

    @Before
    public void setUp() throws Exception {
        observationExportStep = new ObservationExportStep();
        observationExportStep.setJobDefinition(jobDefinition);

        PowerMockito.mockStatic(BatchUtils.class);
        setValuesForMemberFields(observationExportStep,
                "dataSource", dataSource);
        setValuesForMemberFields(observationExportStep, "stepBuilderFactory", stepBuilderFactory);
        setValuesForMemberFields(observationExportStep, "freeMarkerEvaluator", freeMarkerEvaluator);
        setValuesForMemberFields(observationExportStep,
                "observationProcessorFactory", observationProcessorFactory);
        setValuesForMemberFields(observationExportStep,
                "databaseObsWriterObjectFactory", obsWriterObjectFactory);
        setValuesForMemberFields(observationExportStep, "obsIncrementalUpdater", obsIncrementalUpdater);
        BatchUtils.stepNumber = 0;
        when(obsIncrementalUpdater.isMetaDataChanged(any(), anyString())).thenReturn(true);
        when(jobDefinition.getName()).thenReturn(JOB_NAME);
    }

    @Test
    public void shouldSetTheForm() {
        BahmniForm form = mock(BahmniForm.class);
        Concept formName = mock(Concept.class);

        when(form.getFormName()).thenReturn(formName);
        String formWithLenthyName = "moreThanHundredCharacterInTheFormNameMoreThanHundredCharacter" +
                "InTheFormNameMoreThanHundredCharacterInTheFormName";
        when(formName.getName()).thenReturn("Form").thenReturn(formWithLenthyName);

        observationExportStep.setForm(form);

        String stepName = observationExportStep.getStepName("Insertion Step");
        assertEquals("Insertion Step-1 Form", stepName);
        stepName = observationExportStep.getStepName("Insertion Step");
        assertEquals("Insertion Step-2 " +
                "moreThanHundredCharacterInTheFormNameMoreThanHundredCharacterInTheFormNameMoreThanH", stepName);

    }

    @Test
    public void shouldGetTheBatchStepForBaseExport() {
        String formName = "Form";
        TaskletStep expectedBaseExportStep = mock(TaskletStep.class);
        StepBuilder stepBuilder = mock(StepBuilder.class);
        BahmniForm form = mock(BahmniForm.class);
        Concept formNameConcept = mock(Concept.class);
        observationExportStep.setForm(form);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);

        when(form.getFormName()).thenReturn(formNameConcept);
        when(formNameConcept.getName()).thenReturn(formName);
        when(stepBuilderFactory.get("Insertion Step-1 " + formName)).thenReturn(stepBuilder);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(observationProcessorFactory.getObject()).thenReturn(new ObservationProcessor());
        when(obsWriterObjectFactory.getObject()).thenReturn(new DatabaseObsWriter());
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);

        when(simpleStepBuilder.build()).thenReturn(expectedBaseExportStep);

        PowerMockito.mockStatic(JobDefinitionUtil.class);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(any(JobDefinition.class))).thenReturn(true);

        Step observationExportStepStep = observationExportStep.getStep();

        assertNotNull(observationExportStepStep);
        assertEquals(expectedBaseExportStep, observationExportStepStep);
    }

    @Test
    public void shouldCallIncrementalUpdaterForExistingTables() {
        String formName = "FormOne";
        BahmniForm form = mock(BahmniForm.class);
        setUpStepConfig(formName, form);
        observationExportStep.setJobDefinition(jobDefinition);
        when(obsIncrementalUpdater.isMetaDataChanged(formName, JOB_NAME)).thenReturn(false);

        observationExportStep.getStep();

        verify(stepBuilderFactory).get("Insertion Step-1 " + formName);
        verify(obsIncrementalUpdater).isMetaDataChanged(formName, JOB_NAME);
        verify(jobDefinition, atLeastOnce()).getName();
        verify(obsIncrementalUpdater).updateReaderSql("some sql", JOB_NAME, "encounter_id");
    }

    @Test
    public void shouldNotCallIncrementalUpdaterForNonExistingTables() {
        String formName = "FormTwo";
        BahmniForm form = mock(BahmniForm.class);
        setUpStepConfig(formName, form);
        when(obsIncrementalUpdater.isMetaDataChanged(formName, JOB_NAME)).thenReturn(true);

        observationExportStep.getStep();

        verify(stepBuilderFactory).get("Insertion Step-1 " + formName);
        verify(jobDefinition).getName();
        verify(obsIncrementalUpdater).isMetaDataChanged(formName, JOB_NAME);
        verify(obsIncrementalUpdater, never()).updateReaderSql(anyString(), anyString(), anyString());
    }

    private void setUpStepConfig(String formName, BahmniForm form) {
        StepBuilder stepBuilder = mock(StepBuilder.class);
        Concept formNameConcept = mock(Concept.class);
        observationExportStep.setForm(form);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);

        when(form.getFormName()).thenReturn(formNameConcept);
        when(formNameConcept.getName()).thenReturn(formName);
        when(stepBuilderFactory.get("Insertion Step-1 " + formName)).thenReturn(stepBuilder);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(observationProcessorFactory.getObject()).thenReturn(new ObservationProcessor());
        when(obsWriterObjectFactory.getObject()).thenReturn(new DatabaseObsWriter());
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);
        when(freeMarkerEvaluator.evaluate("obsWithParentSql.ftl", form, false)).thenReturn("some sql");
    }
}
