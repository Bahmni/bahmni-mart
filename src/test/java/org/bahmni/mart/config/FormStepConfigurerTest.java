package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.beans.factory.ObjectFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormStepConfigurerTest {

    private ObsStepConfigurer formStepConfigurer;

    @Mock
    private TableGeneratorStep tableGeneratorStep;

    @Mock
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Mock
    private FormListProcessor formListProcessor;

    @Mock
    private ObjectFactory<ObservationExportStep> observationExportStepFactory;

    @Mock
    private FlowBuilder<FlowJobBuilder> completeDataExport;

    @Mock
    private ObsService obsService;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        formStepConfigurer = new FormStepConfigurer();
        setValuesForSuperClassMemberFields(formStepConfigurer, "tableGeneratorStep", tableGeneratorStep);
        setValuesForSuperClassMemberFields(formStepConfigurer,
                "formTableMetadataGenerator", formTableMetadataGenerator);
        setValuesForSuperClassMemberFields(formStepConfigurer,
                "observationExportStepFactory", observationExportStepFactory);
        setValuesForSuperClassMemberFields(formStepConfigurer, "formListProcessor", formListProcessor);
        setValuesForSuperClassMemberFields(formStepConfigurer, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(formStepConfigurer, "obsService", obsService);
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
        verify(observationExportStepFactory,times(2)).getObject();
        verify(medicalHistoryObservationExportStep, times(1)).setForm(medicalHistoryForm);
        verify(fstgObservationExportStep, times(1)).setForm(fstg);
        verify(medicalHistoryObservationExportStep, times(1)).getStep();
        verify(fstgObservationExportStep, times(1)).getStep();
        verify(completeDataExport,times(1)).next(medicalHistoryStep);
        verify(completeDataExport,times(1)).next(fstgStep);
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(medicalHistoryForm);
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(fstg);
    }

    @Test
    public void shouldGetAllFormsUnderAllObservationTemplates() throws Exception {
        List<String> ignoreConcepts = Arrays.asList("video", "image");
        List<Concept> allConcepts = Arrays.asList(new Concept(1, "concept", 1));
        List<BahmniForm> forms = Arrays.asList(new BahmniForm());
        String allObservationTemplates = "All Observation Templates";
        JobDefinition obsJobDefinition = mock(JobDefinition.class);
        when(obsJobDefinition.getType()).thenReturn("obs");
        when(obsJobDefinition.getColumnsToIgnore()).thenReturn(ignoreConcepts);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList(obsJobDefinition));
        when(obsService.getChildConcepts(allObservationTemplates)).thenReturn(allConcepts);
        when(formListProcessor.retrieveAllForms(allConcepts, ignoreConcepts)).thenReturn(forms);

        List<BahmniForm> actual = formStepConfigurer.getAllForms();

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(forms, actual);
        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(obsService, times(1)).getChildConcepts(allObservationTemplates);
        verify(formListProcessor, times(1)).retrieveAllForms(allConcepts, ignoreConcepts);
    }
}