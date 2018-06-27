package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.domain.ForeignKey;
import org.bahmni.mart.table.domain.TableColumn;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

    private static final int BAHMNI_FORM = 0;
    private static final int FSTG_FORM = 1;

    @Mock
    private FlowBuilder<FlowJobBuilder> completeDataExport;

    private StepConfigurer formStepConfigurer;

    @Mock
    private ObservationExportStep fstgObservationExportStep;

    @Mock
    private ObservationExportStep medicalHistoryObservationExportStep;

    @Mock
    private Step medicalHistoryStep;

    @Mock
    private Step fstgStep;

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
        setUpBahmniFormsAndSteps(bahmniForms);

        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(true);

        formStepConfigurer.registerSteps(completeDataExport, jobDefinition);

        verify(formListProcessor, times(1)).retrieveAllForms(any(), any());
        verify(observationExportStepFactory, times(2)).getObject();
        verify(medicalHistoryObservationExportStep, times(1)).setForm(bahmniForms.get(BAHMNI_FORM));
        verify(fstgObservationExportStep, times(1)).setForm(bahmniForms.get(FSTG_FORM));
        verify(medicalHistoryObservationExportStep, times(1)).getStep();
        verify(fstgObservationExportStep, times(1)).getStep();
        verify(completeDataExport, times(1)).next(medicalHistoryStep);
        verify(completeDataExport, times(1)).next(fstgStep);
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(bahmniForms.get(BAHMNI_FORM));
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(bahmniForms.get(FSTG_FORM));
    }

    private void setUpBahmniFormsAndSteps(ArrayList<BahmniForm> bahmniForms) {

        BahmniForm medicalHistoryForm = new BahmniForm();
        BahmniForm fstg = new BahmniForm();
        bahmniForms.add(medicalHistoryForm);
        bahmniForms.add(fstg);

        when(formListProcessor.retrieveAllForms(any(), any())).thenReturn(bahmniForms);
        when(observationExportStepFactory.getObject()).thenReturn(medicalHistoryObservationExportStep)
                .thenReturn(fstgObservationExportStep);
        when(medicalHistoryObservationExportStep.getStep()).thenReturn(medicalHistoryStep);
        when(fstgObservationExportStep.getStep()).thenReturn(fstgStep);
    }

    @Test
    public void shouldRegisterObservationStepsForTwoFormsAndPrimaryForeignKeyConstraintsAreRevoked() {

        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        setUpBahmniFormsAndSteps(bahmniForms);

        List<TableData> tableDataList = getTableData();

        when(formTableMetadataGenerator.getTableData(bahmniForms.get(BAHMNI_FORM)))
                .thenReturn(tableDataList.get(BAHMNI_FORM));
        when(formTableMetadataGenerator.getTableData(bahmniForms.get(FSTG_FORM)))
                .thenReturn(tableDataList.get(FSTG_FORM));

        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(false);

        formStepConfigurer.registerSteps(completeDataExport, jobDefinition);

        verify(formListProcessor, times(1)).retrieveAllForms(any(), any());
        verify(observationExportStepFactory, times(2)).getObject();
        verify(medicalHistoryObservationExportStep, times(1)).setForm(bahmniForms.get(BAHMNI_FORM));
        verify(fstgObservationExportStep, times(1)).setForm(bahmniForms.get(FSTG_FORM));
        verify(medicalHistoryObservationExportStep, times(1)).getStep();
        verify(fstgObservationExportStep, times(1)).getStep();
        verify(completeDataExport, times(1)).next(medicalHistoryStep);
        verify(completeDataExport, times(1)).next(fstgStep);
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(bahmniForms.get(BAHMNI_FORM));
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(bahmniForms.get(FSTG_FORM));
        verify(formTableMetadataGenerator, times(1)).getTableData(bahmniForms.get(BAHMNI_FORM));
        verify(formTableMetadataGenerator, times(1)).getTableData(bahmniForms.get(FSTG_FORM));

        verifyNoPrimaryOrForeignKeyConstraints(tableDataList);
    }

    @Test
    public void shouldGetAllFormsUnderAllObservationTemplates() {
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

    @Test
    public void shouldNotThrowNullPointerExceptionWhileRevokingConstraintsWhenTableDataIsNotPresentInMap() {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        setUpBahmniFormsAndSteps(bahmniForms);

        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(false);
        when(formTableMetadataGenerator.getTableData(any())).thenReturn(null);

        formStepConfigurer.registerSteps(completeDataExport, jobDefinition);
    }

    private void verifyNoPrimaryOrForeignKeyConstraints(List<TableData> tableDataList) {
        tableDataList.forEach(tableData -> tableData.getColumns().forEach(tableColumn -> {
            assertFalse(tableColumn.isPrimaryKey());
            assertNull(tableColumn.getReference());
        }));
    }

    private List<TableData> getTableData() {

        String medicalHistoryFormName = "medical_history";
        String medicalHistoryPrimaryKey = "id_medical_hostory";

        TableData medicalHistoryTableData = new TableData(medicalHistoryFormName);
        TableColumn idMedicalHistory = new TableColumn(medicalHistoryPrimaryKey, "int", true, null);
        TableColumn someColumnInMedicalHistory = new TableColumn("some column", "int", false, null);
        medicalHistoryTableData.setColumns(Arrays.asList(idMedicalHistory, someColumnInMedicalHistory));

        TableData fstgTableData = new TableData("fstg");
        ForeignKey foreignKeyToMedicalHistory = new ForeignKey(medicalHistoryPrimaryKey, medicalHistoryFormName);
        TableColumn foreignKeyColumn = new TableColumn("id_medical_history", "int", false, foreignKeyToMedicalHistory);
        TableColumn someColumnInFstg = new TableColumn("some column", "int", false, null);
        fstgTableData.setColumns(Arrays.asList(foreignKeyColumn, someColumnInFstg));

        return Arrays.asList(medicalHistoryTableData, fstgTableData);
    }
}