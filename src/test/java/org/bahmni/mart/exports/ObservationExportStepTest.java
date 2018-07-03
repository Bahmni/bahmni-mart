package org.bahmni.mart.exports;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.ObservationProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.helper.IncrementalUpdater;
import org.bahmni.mart.table.FormTableMetadataGenerator;
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
    private IncrementalUpdater incrementalUpdater;

    @Mock
    private FormTableMetadataGenerator formTableMetadataGenerator;

    private ObservationExportStep observationExportStep = new ObservationExportStep();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(BatchUtils.class);
        setValuesForMemberFields(observationExportStep,
                "dataSource", dataSource);
        setValuesForMemberFields(observationExportStep, "stepBuilderFactory", stepBuilderFactory);
        setValuesForMemberFields(observationExportStep, "freeMarkerEvaluator", freeMarkerEvaluator);
        setValuesForMemberFields(observationExportStep,
                "observationProcessorFactory", observationProcessorFactory);
        setValuesForMemberFields(observationExportStep,
                "databaseObsWriterObjectFactory", obsWriterObjectFactory);
        setValuesForMemberFields(observationExportStep, "incrementalUpdater", incrementalUpdater);
        setValuesForMemberFields(observationExportStep, "formTableMetadataGenerator", formTableMetadataGenerator);
        BatchUtils.stepNumber = 0;
        when(incrementalUpdater.isMetaDataChanged(any())).thenReturn(true);

    }

    @Test
    public void shouldSetTheForm() {
        BahmniForm form = mock(BahmniForm.class);
        Concept formName = mock(Concept.class);

        when(form.getFormName()).thenReturn(formName);
        String formWithLenthyName = "moreThanHundredCharacterInTheFormNamemoreThanHundredC" +
                "haracterInTheFormNamemoreThanHundredCharacterInTheFormName";
        when(formName.getName()).thenReturn("Form").thenReturn(formWithLenthyName);

        observationExportStep.setForm(form);

        String stepName = observationExportStep.getStepName();
        assertEquals("Step-1 Form", stepName);
        stepName = observationExportStep.getStepName();
        assertEquals("Step-2 moreThanHundredCharacterInTheFormNamemoreThanHundredCharacte" +
                "rInTheFormNamemoreThanHundredChar", stepName);

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
        when(stepBuilderFactory.get("Step-1 " + formName)).thenReturn(stepBuilder);
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
        JobDefinition jobDefinition = new JobDefinition();
        String jobName = "job Name";
        jobDefinition.setName(jobName);
        observationExportStep.setJobDefinition(jobDefinition);
        when(incrementalUpdater.isMetaDataChanged(formName)).thenReturn(false);

        observationExportStep.getStep();

        verify(stepBuilderFactory).get("Step-1 " + formName);
        verify(incrementalUpdater).isMetaDataChanged(formName);
        verify(incrementalUpdater).updateReaderSql("some sql", jobName, "encounter_id");
    }

    @Test
    public void shouldNotCallIncrementalUpdaterForNonExistingTables() {
        String formName = "FormTwo";
        BahmniForm form = mock(BahmniForm.class);
        setUpStepConfig(formName, form);
        when(incrementalUpdater.isMetaDataChanged(formName)).thenReturn(true);

        observationExportStep.getStep();

        verify(stepBuilderFactory).get("Step-1 " + formName);
        verify(incrementalUpdater).isMetaDataChanged(formName);
        verify(incrementalUpdater, never()).updateReaderSql(anyString(), anyString(), anyString());
    }

    private void setUpStepConfig(String formName, BahmniForm form) {
        StepBuilder stepBuilder = mock(StepBuilder.class);
        Concept formNameConcept = mock(Concept.class);
        observationExportStep.setForm(form);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);

        when(form.getFormName()).thenReturn(formNameConcept);
        when(formNameConcept.getName()).thenReturn(formName);
        when(stepBuilderFactory.get("Step-1 " + formName)).thenReturn(stepBuilder);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(observationProcessorFactory.getObject()).thenReturn(new ObservationProcessor());
        when(obsWriterObjectFactory.getObject()).thenReturn(new DatabaseObsWriter());
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);
        when(freeMarkerEvaluator.evaluate("obsWithParentSql.ftl", form)).thenReturn("some sql");
    }
}
