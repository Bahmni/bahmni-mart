package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.Form1ObservationExportStep;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.exports.updatestrategy.IncrementalUpdateStrategy;
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

import static org.bahmni.mart.CommonTestHelper.setValuesForSuperSuperClassMemberFields;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

    @Mock
    private Form1ObservationExportStep fstgForm1ObservationExportStep;

    @Mock
    private Form1ObservationExportStep medicalHistoryForm1ObservationExportStep;

    @Mock
    private Step medicalHistoryStep;

    @Mock
    private Step fstgStep;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private IncrementalStrategyContext incrementalStrategyContext;

    @Mock
    private IncrementalUpdateStrategy incrementalUpdateStrategy;

    @Mock
    private TableData tableData;

    private Form1StepConfigurer formStepConfigurer;

    @Before
    public void setUp() throws Exception {
        mockStatic(JobDefinitionUtil.class);

        formStepConfigurer = new FormStepConfigurer(formTableMetadataGenerator);
        setUp(formStepConfigurer);
        when(jobDefinition.getLocale()).thenReturn("locale");

        setValuesForSuperSuperClassMemberFields(formStepConfigurer, "incrementalStrategyContext",
                incrementalStrategyContext);
        when(incrementalStrategyContext.getStrategy(anyString())).thenReturn(incrementalUpdateStrategy);
        when(incrementalUpdateStrategy.isMetaDataChanged(anyString(), anyString())).thenReturn(true);
        when(formTableMetadataGenerator.getTableData(any(BahmniForm.class))).thenReturn(tableData);
    }

    @Test
    public void shouldCallCreateTablesForObs() {
        List<TableData> tableDataList = new ArrayList<>();
        when(formTableMetadataGenerator.getTableDataList()).thenReturn(tableDataList);

        formStepConfigurer.createTables(jobDefinition);

        verify(tableGeneratorStep, times(1)).createTables(tableDataList, jobDefinition);
    }

    @Test
    public void shouldGenerateMetaDataForForms() {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        setUpBahmniFormsAndSteps(bahmniForms);
        when(JobDefinitionUtil.getJobDefinitionByType(any(), any())).thenReturn(jobDefinition);

        formStepConfigurer.generateTableData(jobDefinition);

        verify(formTableMetadataGenerator).addMetadataForForm(bahmniForms.get(BAHMNI_FORM));
        verify(formTableMetadataGenerator).addMetadataForForm(bahmniForms.get(FSTG_FORM));
        verify(jobDefinition).getLocale();
    }

    @Test
    public void shouldDropConstraintsWhenIsAddMoreMultiSelectEnabledIsFalse() {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        setUpBahmniFormsAndSteps(bahmniForms);
        when(JobDefinitionUtil.getJobDefinitionByType(any(), any())).thenReturn(jobDefinition);
        List<TableData> tableDataList = getTableData();
        when(formTableMetadataGenerator.getTableData(bahmniForms.get(BAHMNI_FORM)))
                .thenReturn(tableDataList.get(BAHMNI_FORM));
        when(formTableMetadataGenerator.getTableData(bahmniForms.get(FSTG_FORM)))
                .thenReturn(tableDataList.get(FSTG_FORM));
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(false);

        formStepConfigurer.generateTableData(jobDefinition);

        verifyNoPrimaryOrForeignKeyConstraints(tableDataList);
    }

    @Test
    public void shouldNotDropConstraintsWhenIsAddMoreMultiSelectEnabledIsTrue() {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        BahmniForm medicalHistoryForm = new BahmniForm();
        bahmniForms.add(medicalHistoryForm);
        when(formListProcessor.retrieveAllForms(any(), any())).thenReturn(bahmniForms);
        when(observationExportStepFactory.getObject()).thenReturn(medicalHistoryObservationExportStep);
        when(medicalHistoryObservationExportStep.getStep()).thenReturn(medicalHistoryStep);
        when(JobDefinitionUtil.getJobDefinitionByType(any(), any())).thenReturn(jobDefinition);
        String medicalHistoryFormName = "medical_history";
        String medicalHistoryPrimaryKey = "id_medical_history";
        TableData medicalHistoryTableData = new TableData(medicalHistoryFormName);
        TableColumn idMedicalHistory = new TableColumn(medicalHistoryPrimaryKey, "int", true, null);
        TableColumn someColumnInMedicalHistory = new TableColumn("some column", "int", false, null);
        medicalHistoryTableData.setColumns(Arrays.asList(idMedicalHistory, someColumnInMedicalHistory));
        List<TableData> tableDataList = Arrays.asList(medicalHistoryTableData);
        when(formTableMetadataGenerator.getTableData(bahmniForms.get(BAHMNI_FORM)))
                .thenReturn(tableDataList.get(BAHMNI_FORM));
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(true);

        formStepConfigurer.generateTableData(jobDefinition);
        tableDataList.forEach(tableData -> tableData.getColumns().forEach(tableColumn -> {
            if ("id_medical_history".equals(tableColumn.getName())) {
                assertTrue(tableColumn.isPrimaryKey());
            }
        }));
    }

    @Test
    public void shouldRegisterObservationStepsForTwoForms() throws Exception {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        setUpBahmniFormsAndSteps(bahmniForms);
        setValuesForSuperSuperClassMemberFields(formStepConfigurer, "allForms", bahmniForms);

        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(true);

        formStepConfigurer.registerSteps(completeDataExport, jobDefinition);

        verify(observationExportStepFactory, times(2)).getObject();
        verify(medicalHistoryForm1ObservationExportStep, times(1)).setForm(bahmniForms.get(BAHMNI_FORM));
        verify(fstgForm1ObservationExportStep, times(1)).setForm(bahmniForms.get(FSTG_FORM));
        verify(medicalHistoryForm1ObservationExportStep, times(1)).getStep();
        verify(fstgForm1ObservationExportStep, times(1)).getStep();
        verify(completeDataExport, times(1)).next(medicalHistoryStep);
        verify(completeDataExport, times(1)).next(fstgStep);
    }

    @Test
    public void shouldRegisterObservationStepsForTwoFormsAndPrimaryForeignKeyConstraintsAreRevoked() throws Exception {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        setUpBahmniFormsAndSteps(bahmniForms);
        setValuesForSuperSuperClassMemberFields(formStepConfigurer, "allForms", bahmniForms);
        List<TableData> tableDataList = getTableData();
        when(formTableMetadataGenerator.getTableData(bahmniForms.get(BAHMNI_FORM)))
                .thenReturn(tableDataList.get(BAHMNI_FORM));
        when(formTableMetadataGenerator.getTableData(bahmniForms.get(FSTG_FORM)))
                .thenReturn(tableDataList.get(FSTG_FORM));
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(false);

        formStepConfigurer.registerSteps(completeDataExport, jobDefinition);

        verify(observationExportStepFactory, times(2)).getObject();
        verify(medicalHistoryForm1ObservationExportStep, times(1)).setForm(bahmniForms.get(BAHMNI_FORM));
        verify(fstgForm1ObservationExportStep, times(1)).setForm(bahmniForms.get(FSTG_FORM));
        verify(medicalHistoryForm1ObservationExportStep, times(1)).getStep();
        verify(fstgForm1ObservationExportStep, times(1)).getStep();
        verify(completeDataExport, times(1)).next(medicalHistoryStep);
        verify(completeDataExport, times(1)).next(fstgStep);
        verify(formTableMetadataGenerator, times(1)).getTableData(bahmniForms.get(BAHMNI_FORM));
        verify(formTableMetadataGenerator, times(1)).getTableData(bahmniForms.get(FSTG_FORM));
    }

    @Test
    public void shouldGetAllFormsUnderAllObservationTemplates() {
        List<String> ignoreConcepts = Arrays.asList("video", "image");
        List<Concept> allConcepts = Collections.singletonList(new Concept(1, "concept", 1));
        List<BahmniForm> forms = Collections.singletonList(new BahmniForm());
        String allObservationTemplates = "All Observation Templates";
        when(jobDefinition.getType()).thenReturn("obs");
        when(jobDefinition.getColumnsToIgnore()).thenReturn(ignoreConcepts);
        List<JobDefinition> jobDefinitions = Collections.singletonList(jobDefinition);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        when(getJobDefinitionByType(jobDefinitions, "obs")).thenReturn(jobDefinition);
        when(conceptService.getChildConcepts(allObservationTemplates, "locale")).thenReturn(allConcepts);
        when(formListProcessor.retrieveAllForms(allConcepts, jobDefinition)).thenReturn(forms);

        List<BahmniForm> actual = formStepConfigurer.getAllForms();

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(forms, actual);
        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verifyStatic(times(1));
        getJobDefinitionByType(jobDefinitions, "obs");
        verify(conceptService, times(1)).getChildConcepts(allObservationTemplates, "locale");
        verify(formListProcessor, times(1)).retrieveAllForms(allConcepts, jobDefinition);
    }

    @Test
    public void shouldNotThrowNullPointerExceptionWhileRevokingConstraintsWhenTableDataIsNotPresentInMap()
            throws Exception {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        setUpBahmniFormsAndSteps(bahmniForms);
        setValuesForSuperSuperClassMemberFields(formStepConfigurer, "allForms", bahmniForms);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(false);
        when(formTableMetadataGenerator.getTableData(any())).thenReturn(null);

        formStepConfigurer.registerSteps(completeDataExport, jobDefinition);
    }

    private void setUpBahmniFormsAndSteps(ArrayList<BahmniForm> bahmniForms) {
        BahmniForm medicalHistoryForm = new BahmniForm();
        BahmniForm fstg = new BahmniForm();
        bahmniForms.add(medicalHistoryForm);
        bahmniForms.add(fstg);

        when(formListProcessor.retrieveAllForms(any(), any())).thenReturn(bahmniForms);
        when(observationExportStepFactory.getObject()).thenReturn(medicalHistoryForm1ObservationExportStep)
                .thenReturn(fstgForm1ObservationExportStep);
        when(medicalHistoryForm1ObservationExportStep.getStep()).thenReturn(medicalHistoryStep);
        when(fstgForm1ObservationExportStep.getStep()).thenReturn(fstgStep);
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
