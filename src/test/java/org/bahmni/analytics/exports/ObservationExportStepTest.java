package org.bahmni.analytics.exports;

import org.bahmni.analytics.BatchUtils;
import org.bahmni.analytics.CommonTestHelper;
import org.bahmni.analytics.form.ObservationProcessor;
import org.bahmni.analytics.form.domain.BahmniForm;
import org.bahmni.analytics.form.domain.Concept;
import org.bahmni.analytics.helper.FreeMarkerEvaluator;
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

    private ObservationExportStep observationExportStep = new ObservationExportStep();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(BatchUtils.class);
        CommonTestHelper.setValuesForMemberFields(observationExportStep,
                "dataSource", dataSource);
        CommonTestHelper.setValuesForMemberFields(observationExportStep, "stepBuilderFactory", stepBuilderFactory);
        CommonTestHelper.setValuesForMemberFields(observationExportStep, "freeMarkerEvaluator", freeMarkerEvaluator);
        CommonTestHelper.setValuesForMemberFields(observationExportStep,
                "observationProcessorFactory", observationProcessorFactory);
        CommonTestHelper.setValuesForMemberFields(observationExportStep,
                "databaseObsWriterObjectFactory", obsWriterObjectFactory);
        BatchUtils.stepNumber = 0;
    }

    @Test
    public void shouldSetTheForm() throws Exception {
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
    public void shouldGetTheBatchStepForBaseExport() throws Exception {
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

        Step observationExportStepStep = observationExportStep.getStep();

        assertNotNull(observationExportStepStep);
        assertEquals(expectedBaseExportStep, observationExportStepStep);
    }
}
