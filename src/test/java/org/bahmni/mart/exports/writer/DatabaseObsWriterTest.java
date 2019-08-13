package org.bahmni.mart.exports.writer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.ObsRecordExtractorForTable;
import org.bahmni.mart.exports.updatestrategy.ObsIncrementalUpdateStrategy;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.Form2TableMetadataGenerator;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseObsWriterTest {

    private DatabaseObsWriter databaseObsWriter;
    private static final String JOB_NAME = "jobName";

    @Mock
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Mock
    private Form2TableMetadataGenerator form2TableMetadataGenerator;

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private FreeMarkerEvaluator freeMarkerEvaluatorForTableRecords;

    @Mock
    private ObsIncrementalUpdateStrategy obsIncrementalUpdater;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        databaseObsWriter = new DatabaseObsWriter();
        setValuesForMemberFields(databaseObsWriter, "formTableMetadataGenerator",
                formTableMetadataGenerator);
        setValuesForMemberFields(databaseObsWriter, "form2TableMetadataGenerator",
                form2TableMetadataGenerator);
        setValuesForSuperClassMemberFields(databaseObsWriter, "martJdbcTemplate", martJdbcTemplate);
        setValuesForMemberFields(databaseObsWriter, "freeMarkerEvaluatorForTableRecords",
                freeMarkerEvaluatorForTableRecords);
    }


    @Test
    public void shouldInsertDataInAnalyticsDatabase() {
        BahmniForm bahmniForm = new BahmniForm();
        Concept formName = new Concept(1, "test", 1);
        formName.setDataType("N/A");
        bahmniForm.setFormName(formName);
        Concept fieldOne = new Concept(1, "field_one", 0);
        fieldOne.setDataType("Numeric");

        Concept fieldTwo = new Concept(1, "field_two", 0);
        fieldTwo.setDataType("Text");

        bahmniForm.addField(fieldOne);
        bahmniForm.addField(fieldTwo);

        ArrayList<ArrayList<Obs>> items = new ArrayList<>();
        ArrayList<Obs> obsList = new ArrayList<>();
        Obs obs1 = new Obs(1, 2, fieldOne, "4");
        obs1.setEncounterId("56");

        Obs obs2 = new Obs(2, 2, fieldTwo, "test IT");
        obs2.setEncounterId("56");

        obsList.add(obs1);
        obsList.add(obs2);

        items.add(obsList);

        databaseObsWriter.setForm(bahmniForm);
        databaseObsWriter.setJobDefinition(jobDefinition);
        when(formTableMetadataGenerator.getTableData(bahmniForm)).thenReturn(new TableData("test"));
        when(freeMarkerEvaluatorForTableRecords.evaluate(anyString(), any(ObsRecordExtractorForTable.class)))
                .thenReturn("some sql");

        databaseObsWriter.write(items);

        verify(martJdbcTemplate).execute("some sql");
    }

    @Test
    public void shouldInsertDataInAnalyticsDatabaseForForms2() {
        BahmniForm bahmniForm = new BahmniForm();
        Concept formName = new Concept(1, "test", 1);
        formName.setDataType("N/A");
        bahmniForm.setFormName(formName);
        Concept fieldOne = new Concept(1, "field_one", 0);
        fieldOne.setDataType("Numeric");

        Concept fieldTwo = new Concept(1, "field_two", 0);
        fieldTwo.setDataType("Text");

        bahmniForm.addField(fieldOne);
        bahmniForm.addField(fieldTwo);

        ArrayList<ArrayList<Obs>> items = new ArrayList<>();
        ArrayList<Obs> obsList = new ArrayList<>();
        Obs obs1 = new Obs(1, 2, fieldOne, "4");
        obs1.setEncounterId("56");

        Obs obs2 = new Obs(2, 2, fieldTwo, "test IT");
        obs2.setEncounterId("56");

        obsList.add(obs1);
        obsList.add(obs2);

        items.add(obsList);

        databaseObsWriter.setForm(bahmniForm);
        databaseObsWriter.setJobDefinition(jobDefinition);
        when(jobDefinition.getType()).thenReturn("form2obs");
        when(form2TableMetadataGenerator.getTableData(bahmniForm)).thenReturn(new TableData("test"));
        when(freeMarkerEvaluatorForTableRecords.evaluate(anyString(), any(ObsRecordExtractorForTable.class)))
                .thenReturn("some sql");

        databaseObsWriter.write(items);

        verify(martJdbcTemplate).execute("some sql");
    }
}
