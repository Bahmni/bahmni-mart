package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormStepConfigurerTest extends StepConfigurerTestHelper {
    @Mock
    private FlowBuilder<FlowJobBuilder> completeDataExport;

    private StepConfigurer formStepConfigurer;

    @Before
    public void setUp() throws Exception {
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

    @Test
    public void shouldGetAllFormsUnderAllObservationTemplatesByFilteringFormsWhichHaveDuplicateConcepts()
            throws NoSuchFieldException, IllegalAccessException {

        Logger logger = mock(Logger.class);
        setValueForFinalStaticField(FormStepConfigurer.class, "logger", logger);

        BahmniForm bahmniFormWithDuplicateConcepts = mock(BahmniForm.class);
        BahmniForm bahmniFormWithUniqueConcepts = mock(BahmniForm.class);

        List<BahmniForm> forms = Arrays.asList(bahmniFormWithDuplicateConcepts, bahmniFormWithUniqueConcepts);

        String duplicateFormName = "Duplicate form name";
        Concept duplicateFormConcept = new Concept(0, duplicateFormName, 1);

        String uniqueFormName = "Unique form name";
        Concept uniqueFormConcept = new Concept(0, uniqueFormName, 1);

        Concept concept1 = new Concept(1, "concept1", 0);
        Concept duplicateConcept = new Concept(1, "concept1", 0);
        Concept concept3 = new Concept(1, "concept3", 0);

        JobDefinition obsJobDefinition = mock(JobDefinition.class);
        when(obsJobDefinition.getType()).thenReturn("obs");
        when(obsJobDefinition.getColumnsToIgnore()).thenReturn(null);

        List<Concept> allConceptsUnderDuplicateConceptsForm = Arrays.asList(concept1, duplicateConcept, concept3);
        List<Concept> allConceptsUnderUniqueConceptsForm = Arrays.asList(concept1, concept3);

        String allObservationTemplates = "All Observation Templates";
        List<Concept> formConcepts = Arrays.asList(duplicateFormConcept, uniqueFormConcept);

        when(obsService.getChildConcepts(allObservationTemplates)).thenReturn(formConcepts);
        when(formListProcessor.retrieveAllForms(formConcepts, new ArrayList<>())).thenReturn(forms);

        when(bahmniFormWithDuplicateConcepts.getFields()).thenReturn(allConceptsUnderDuplicateConceptsForm);
        when(bahmniFormWithUniqueConcepts.getFields()).thenReturn(allConceptsUnderUniqueConceptsForm);


        when(bahmniFormWithDuplicateConcepts.getFormName()).thenReturn(duplicateFormConcept);
        when(bahmniFormWithUniqueConcepts.getFormName()).thenReturn(uniqueFormConcept);

        List<BahmniForm> allForms = formStepConfigurer.getAllForms();

        assertEquals(1, allForms.size());
        BahmniForm uniqueForm = allForms.get(0);
        assertEquals(2, uniqueForm.getFields().size());

        assertEquals(uniqueFormName, uniqueForm.getFormName().getName());
        containsInAnyOrder(Arrays.asList(concept1, concept3), uniqueForm.getFields());
        verify(logger, times(1))
                .warn("Skipping the form 'Duplicate form name' since it has duplicate concepts 'concept1'");

        verify(obsService, times(1)).getChildConcepts(allObservationTemplates);
        verify(formListProcessor, times(1)).retrieveAllForms(formConcepts, new ArrayList<>());

    }
}