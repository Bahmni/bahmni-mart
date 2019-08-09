package org.bahmni.mart.exports;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.Form2ObsIncrementalStrategy;
import org.bahmni.mart.exports.writer.DatabaseObsWriter;
import org.bahmni.mart.exports.writer.RemovalWriter;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form2.Form2ObservationProcessor;
import org.bahmni.mart.table.Form2TableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.ObjectFactory;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({BatchUtils.class, JobDefinitionUtil.class})
@RunWith(PowerMockRunner.class)
public class Form2ObservationExportStepTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private ObjectFactory<Form2ObservationProcessor> observationProcessorFactory;

    @Mock
    private ObjectFactory<DatabaseObsWriter> obsWriterObjectFactory;

    @Mock
    private ObjectFactory<RemovalWriter> removalWriterObjectFactory;

    @Mock
    private Form2TableMetadataGenerator form2TableMetadataGenerator;

    @Mock
    private Form2ObsIncrementalStrategy obsIncrementalStrategy;

    private Form2ObservationExportStep form2ObservationExportStep;
    private static final String JOB_NAME = "job Name";

    @Before
    public void setUp() throws Exception {
        form2ObservationExportStep = new Form2ObservationExportStep();
        form2ObservationExportStep.setJobDefinition(jobDefinition);
        mockStatic(BatchUtils.class);
        BatchUtils.stepNumber = 0;
        setValuesForMemberFields(form2ObservationExportStep, "dataSource", dataSource);
        setValuesForMemberFields(form2ObservationExportStep, "stepBuilderFactory", stepBuilderFactory);
        setValuesForMemberFields(form2ObservationExportStep, "obsIncrementalUpdater", obsIncrementalStrategy);
        setValuesForMemberFields(form2ObservationExportStep,
                "observationProcessorFactory", observationProcessorFactory);
        setValuesForMemberFields(form2ObservationExportStep,
                "databaseObsWriterObjectFactory", obsWriterObjectFactory);
        setValuesForMemberFields(form2ObservationExportStep,
                "removalWriterObjectFactory",removalWriterObjectFactory);
        setValuesForMemberFields(form2ObservationExportStep,
                "form2TableMetadataGenerator",form2TableMetadataGenerator);
        when(jobDefinition.getName()).thenReturn(JOB_NAME);
    }

    @Test
    public void shouldSetTheForm() {
        BahmniForm form = mock(BahmniForm.class);
        Concept formName = mock(Concept.class);

        when(form.getFormName()).thenReturn(formName);
        String formWithLengthyName = "moreThanHundredCharacterInTheFormNameMoreThanHundredCharacter" +
                "InTheFormNameMoreThanHundredCharacterInTheFormNameMoreThanHundredCharacterInTheFormName";
        when(formName.getName()).thenReturn("Form").thenReturn(formWithLengthyName);

        form2ObservationExportStep.setForm(form);

        String stepName = form2ObservationExportStep.getStepName("Insertion Step");
        assertEquals("Insertion Step-1 Form", stepName);
        stepName = form2ObservationExportStep.getStepName("Insertion Step");
        assertEquals("Insertion Step-2 " +
                "moreThanHundredCharacterInTheFormNameMoreThanHundredCharacterInTheFormNameMoreThanH", stepName);
    }

    @Test
    public void shouldGetTheBatchStepForBaseExport() {
        String formName = "FormName";
        TaskletStep expectedBaseExportStep = mock(TaskletStep.class);
        StepBuilder stepBuilder = mock(StepBuilder.class);
        Form2ObsIncrementalStrategy obsIncrementalUpdater = mock(Form2ObsIncrementalStrategy.class);
        BahmniForm form = mock(BahmniForm.class);
        Concept formNameConcept = mock(Concept.class);
        form2ObservationExportStep.setForm(form);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);

        when(form.getFormName()).thenReturn(formNameConcept);
        when(formNameConcept.getName()).thenReturn(formName);
        when(stepBuilderFactory.get("Insertion Step-1 " + formName)).thenReturn(stepBuilder);
        when(stepBuilder.chunk(500)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(observationProcessorFactory.getObject()).thenReturn(new Form2ObservationProcessor());
        when(obsWriterObjectFactory.getObject()).thenReturn(new DatabaseObsWriter());
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);

        when(simpleStepBuilder.build()).thenReturn(expectedBaseExportStep);

        mockStatic(JobDefinitionUtil.class);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(any(JobDefinition.class))).thenReturn(true);

        Step observationExportStepStep = form2ObservationExportStep.getStep();

        verify(stepBuilderFactory).get("Insertion Step-1 " + formName);
        assertNotNull(observationExportStepStep);
        assertEquals(expectedBaseExportStep, observationExportStepStep);
    }

    @Test
    public void shouldCallBatchUtilsMethodsToReplaceParametersInReaderSQL() throws Exception {
        String formName = "FormOne";
        BahmniForm form = mock(BahmniForm.class);
        setUpStepConfig(formName, form);
        when(form.getDepthToParent()).thenReturn(0);
        String concept1Name = "HI, concept1";
        String concept2Name = "PI, concept2";
        Map<String, String> fieldNameAndFullySpecifiedNameMap = new HashMap<>();
        fieldNameAndFullySpecifiedNameMap.put(concept1Name, concept1Name);
        fieldNameAndFullySpecifiedNameMap.put(concept2Name, concept2Name);
        when(form.getFieldNameAndFullySpecifiedNameMap()).thenReturn(fieldNameAndFullySpecifiedNameMap);
        when(jobDefinition.getLocale()).thenReturn("fr");
        when(jobDefinition.getConceptReferenceSource()).thenReturn("WHO");
        String obsReadersSql = "obs reader sql";
        setValuesForMemberFields(form2ObservationExportStep, "obsReaderSql", obsReadersSql);
        when(observationProcessorFactory.getObject()).thenReturn(new Form2ObservationProcessor());
        when(obsWriterObjectFactory.getObject()).thenReturn(new DatabaseObsWriter());
        when(BatchUtils.constructSqlWithParameter(anyObject(), anyObject(), anyString())).thenReturn(obsReadersSql);
        when(BatchUtils.constructSqlWithParameter(anyObject(), anyObject(), anyList())).thenReturn(obsReadersSql);
        when(BatchUtils.constructSqlWithParameter(anyObject(), anyObject(), anyBoolean())).thenReturn(obsReadersSql);

        form2ObservationExportStep.getStep();

        verify(form, times(3)).getFormName();
        verify(form.getFormName(), times(3)).getName();
        verify(form).getFieldNameAndFullySpecifiedNameMap();
        verify(jobDefinition).getConceptReferenceSource();
        verifyStatic();
        BatchUtils.constructSqlWithParameter(obsReadersSql, "formName", "FormOne");
        verifyStatic();
        BatchUtils.constructSqlWithParameter(obsReadersSql, "conceptNames",
                Arrays.asList(concept2Name, concept1Name));
        verifyStatic();
        BatchUtils.constructSqlWithParameter(obsReadersSql, "voided", false);
        verifyStatic();
        BatchUtils.constructSqlWithParameter(obsReadersSql, "conceptReferenceSource", "WHO");

    }

    @Test
    public void shouldCallBatchUtilsMethodsToReplaceParametersInReaderSQLForChildForm() throws Exception {
        String rootFormName = "FormOne";
        BahmniForm rootForm = mock(BahmniForm.class);
        Concept rootFormNameConcept = mock(Concept.class);
        when(rootForm.getFormName()).thenReturn(rootFormNameConcept);
        when(rootFormNameConcept.getName()).thenReturn(rootFormName);

        String formName = "SectionAddMore";
        BahmniForm form = mock(BahmniForm.class);
        setUpStepConfig(formName, form);
        when(form.getDepthToParent()).thenReturn(1);
        when(form.getRootForm()).thenReturn(rootForm);

        String concept1Name = "HI, concept1";
        String concept2Name = "PI, concept2";
        Map<String, String> fieldNameAndFullySpecifiedNameMap = new HashMap<>();
        fieldNameAndFullySpecifiedNameMap.put(concept1Name, concept1Name);
        fieldNameAndFullySpecifiedNameMap.put(concept2Name, concept2Name);
        when(form.getFieldNameAndFullySpecifiedNameMap()).thenReturn(fieldNameAndFullySpecifiedNameMap);
        when(jobDefinition.getConceptReferenceSource()).thenReturn("WHO");
        String obsReadersSql = "obs reader sql";
        setValuesForMemberFields(form2ObservationExportStep, "obsReaderSql", obsReadersSql);
        when(observationProcessorFactory.getObject()).thenReturn(new Form2ObservationProcessor());
        when(obsWriterObjectFactory.getObject()).thenReturn(new DatabaseObsWriter());
        when(BatchUtils.constructSqlWithParameter(anyObject(), anyObject(), anyString())).thenReturn(obsReadersSql);
        when(BatchUtils.constructSqlWithParameter(anyObject(), anyObject(), anyList())).thenReturn(obsReadersSql);
        when(BatchUtils.constructSqlWithParameter(anyObject(), anyObject(), anyBoolean())).thenReturn(obsReadersSql);

        form2ObservationExportStep.getStep();

        verify(form, times(2)).getFormName();
        verify(form.getFormName(), times(2)).getName();
        verify(rootForm, times(1)).getFormName();
        verify(rootForm.getFormName(), times(1)).getName();
        verify(form).getFieldNameAndFullySpecifiedNameMap();
        verify(jobDefinition).getConceptReferenceSource();
        verifyStatic();
        BatchUtils.constructSqlWithParameter(obsReadersSql, "formName", "FormOne");
        verifyStatic();
        BatchUtils.constructSqlWithParameter(obsReadersSql, "conceptNames",
                Arrays.asList(concept2Name, concept1Name));
        verifyStatic();
        BatchUtils.constructSqlWithParameter(obsReadersSql, "voided", false);
        verifyStatic();
        BatchUtils.constructSqlWithParameter(obsReadersSql, "conceptReferenceSource", "WHO");
    }

    @Test
    public void shouldUpdateReaderSqlForIncrementalUpdateWhenNoMetaDataIsChanged() throws Exception {
        String formName = "FormOne";
        BahmniForm form = mock(BahmniForm.class);
        setUpStepConfig(formName, form);
        when(form.getDepthToParent()).thenReturn(0);
        when(jobDefinition.getName()).thenReturn("form2obs");
        String obsReadersSql = "obs reader sql";
        setValuesForMemberFields(form2ObservationExportStep, "obsReaderSql", obsReadersSql);
        when(BatchUtils.constructSqlWithParameter(anyObject(), anyObject(), anyString())).thenReturn(obsReadersSql);
        when(observationProcessorFactory.getObject()).thenReturn(new Form2ObservationProcessor());
        when(obsWriterObjectFactory.getObject()).thenReturn(new DatabaseObsWriter());

        form2ObservationExportStep.getStep();

        verify(obsIncrementalStrategy).updateReaderSql(obsReadersSql, "form2obs", "encounter_id", "FormOne");
    }

    @Test
    public void shouldReturnRemovalStep() throws Exception {
        String formName = "FormOne";
        BahmniForm form = mock(BahmniForm.class);
        Concept formNameConcept = mock(Concept.class);
        when(form.getFormName()).thenReturn(formNameConcept);
        when(formNameConcept.getName()).thenReturn(formName);
        TableData tableData = mock(TableData.class);
        when(jobDefinition.getName()).thenReturn("form2obs");
        String obsReadersSql = "obs reader sql";
        setValuesForMemberFields(form2ObservationExportStep, "form", form);
        setValuesForMemberFields(form2ObservationExportStep, "obsReaderSql", obsReadersSql);
        when(BatchUtils.constructSqlWithParameter(anyObject(), anyObject(), anyString())).thenReturn(obsReadersSql);
        RemovalWriter removalWriter = mock(RemovalWriter.class);
        when(removalWriterObjectFactory.getObject()).thenReturn(removalWriter);
        when(form2TableMetadataGenerator.getTableData(form)).thenReturn(tableData);

        StepBuilder stepBuilder = mock(StepBuilder.class);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
        when(stepBuilderFactory.get("Removal Step-1 " + formName)).thenReturn(stepBuilder);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(removalWriter)).thenReturn(simpleStepBuilder);
        TaskletStep expectedStep = mock(TaskletStep.class);
        when(simpleStepBuilder.build()).thenReturn(expectedStep);

        Step actualRemovalStep = form2ObservationExportStep.getRemovalStep();

        assertEquals(expectedStep, actualRemovalStep);
        verify(removalWriterObjectFactory).getObject();
        verify(removalWriter).setTableData(tableData);
        verify(removalWriter).setJobDefinition(jobDefinition);
    }

    private void setUpStepConfig(String formName, BahmniForm form) {
        StepBuilder stepBuilder = mock(StepBuilder.class);
        Concept formNameConcept = mock(Concept.class);
        form2ObservationExportStep.setForm(form);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);

        when(form.getFormName()).thenReturn(formNameConcept);
        when(formNameConcept.getName()).thenReturn(formName);
        when(stepBuilderFactory.get("Insertion Step-1 " + formName)).thenReturn(stepBuilder);
        when(stepBuilder.chunk(500)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);
    }
}
