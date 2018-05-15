package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobDefinitionUtil.class)
public class FormStepConfigurerTest extends StepConfigurerTestHelper {
    @Mock
    private FlowBuilder<FlowJobBuilder> completeDataExport;

    private StepConfigurer formStepConfigurer;

    @Before
    public void setUp() throws Exception {
        mockStatic(JobDefinitionUtil.class);

        formStepConfigurer = new FormStepConfigurer();
        setUp(formStepConfigurer);
    }

    @Test
    public void shouldCallCreateTables() {
        List<TableData> tableDataList = new ArrayList<>();
        when(formTableMetadataGenerator.getTableDataList()).thenReturn(tableDataList);

        formStepConfigurer.createTables();

        verify(tableGeneratorStep, times(1)).createTables(tableDataList);
    }

    @Test
    public void shouldRegisterObservationStepsForTwoForms() {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();

        BahmniForm medicalHistoryForm = new BahmniForm();
        BahmniForm fstg = new BahmniForm();
        bahmniForms.add(medicalHistoryForm);
        bahmniForms.add(fstg);

        Step medicalHistoryStep = mock(Step.class);
        Step fstgStep = mock(Step.class);

        when(formListProcessor.retrieveAllForms(any(), any())).thenReturn(bahmniForms);
        ObservationExportStep medicalHistoryObservationExportStep = mock(ObservationExportStep.class);
        ObservationExportStep fstgObservationExportStep = mock(ObservationExportStep.class);
        when(observationExportStepFactory.getObject()).thenReturn(medicalHistoryObservationExportStep)
                .thenReturn(fstgObservationExportStep);
        when(medicalHistoryObservationExportStep.getStep()).thenReturn(medicalHistoryStep);
        when(fstgObservationExportStep.getStep()).thenReturn(fstgStep);

        formStepConfigurer.registerSteps(completeDataExport, new JobDefinition());

        verify(formListProcessor, times(1)).retrieveAllForms(any(), any());
        verify(observationExportStepFactory, times(2)).getObject();
        verify(medicalHistoryObservationExportStep, times(1)).setForm(medicalHistoryForm);
        verify(fstgObservationExportStep, times(1)).setForm(fstg);
        verify(medicalHistoryObservationExportStep, times(1)).getStep();
        verify(fstgObservationExportStep, times(1)).getStep();
        verify(completeDataExport, times(1)).next(medicalHistoryStep);
        verify(completeDataExport, times(1)).next(fstgStep);
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(medicalHistoryForm);
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(fstg);
    }

    @Test
    public void shouldGetAllFormsUnderAllObservationTemplates() throws Exception {
        List<String> ignoreConcepts = Arrays.asList("video", "image");
        List<Concept> allConcepts = Collections.singletonList(new Concept(1, "concept", 1));
        List<BahmniForm> forms = Collections.singletonList(new BahmniForm());
        String allObservationTemplates = "All Observation Templates";
        JobDefinition obsJobDefinition = mock(JobDefinition.class);
        when(obsJobDefinition.getType()).thenReturn("obs");
        when(obsJobDefinition.getColumnsToIgnore()).thenReturn(ignoreConcepts);
        List<JobDefinition> jobDefinitions = Collections.singletonList(obsJobDefinition);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        when(getJobDefinitionByType(jobDefinitions, "obs")).thenReturn(obsJobDefinition);
        when(conceptService.getChildConcepts(allObservationTemplates)).thenReturn(allConcepts);
        when(formListProcessor.retrieveAllForms(allConcepts, obsJobDefinition)).thenReturn(forms);

        List<BahmniForm> actual = formStepConfigurer.getAllForms();

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(forms, actual);
        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verifyStatic(times(1));
        getJobDefinitionByType(jobDefinitions, "obs");
        verify(conceptService, times(1)).getChildConcepts(allObservationTemplates);
        verify(formListProcessor, times(1)).retrieveAllForms(allConcepts, obsJobDefinition);
    }
}