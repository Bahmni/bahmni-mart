package org.bahmni.mart.exports;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.SeparateTableConfig;
import org.bahmni.mart.form.BahmniFormFactory;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@PrepareForTest(BatchUtils.class)
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
    private BahmniFormFactory bahmniFormFactory;

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
        setValuesForMemberFields(observationExportStep, "jobDefinition", jobDefinition);
        setValuesForMemberFields(observationExportStep, "bahmniFormFactory", bahmniFormFactory);
        BatchUtils.stepNumber = 0;
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
    public void shouldGetTheBatchStepForBaseExportWhenSeparateTableConfigIsTrue() {
        StepBuilder stepBuilder = mock(StepBuilder.class);
        BahmniForm form = mock(BahmniForm.class);
        Concept formNameConcept = mock(Concept.class);
        String formName = "Form";
        observationExportStep.setForm(form);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
        TaskletStep expectedBaseExportStep = mock(TaskletStep.class);

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
        SeparateTableConfig separateTableConfig = mock(SeparateTableConfig.class);
        when(jobDefinition.getSeparateTableConfig()).thenReturn(separateTableConfig);
        when(separateTableConfig.isEnableForAddMoreAndMultiSelect()).thenReturn(true);

        Step observationExportStepStep = observationExportStep.getStep();

        assertNotNull(observationExportStepStep);
        assertEquals(expectedBaseExportStep, observationExportStepStep);
    }

    @Test
    public void shouldGetTheBatchStepForBaseExportWhenSeparateTableConfigIsFalse() throws Exception {
        StepBuilder stepBuilder = mock(StepBuilder.class);
        BahmniForm form = mock(BahmniForm.class);
        Concept formNameConcept = mock(Concept.class);
        String formName = "Form";
        observationExportStep.setForm(form);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
        TaskletStep expectedBaseExportStep = mock(TaskletStep.class);

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
        SeparateTableConfig separateTableConfig = mock(SeparateTableConfig.class);
        when(jobDefinition.getSeparateTableConfig()).thenReturn(separateTableConfig);

        when(separateTableConfig.isEnableForAddMoreAndMultiSelect()).thenReturn(false);

        BahmniFormFactory bahmniFormFactory = mock(BahmniFormFactory.class);
        setValuesForMemberFields(observationExportStep, "bahmniFormFactory", bahmniFormFactory);
        when(bahmniFormFactory.getFormWithAddMoreAndMultiSelectConceptsAlone(form)).thenReturn(form);

        Step observationExportStepStep = observationExportStep.getStep();

        assertNotNull(observationExportStepStep);
        assertEquals(expectedBaseExportStep, observationExportStepStep);
    }
}
