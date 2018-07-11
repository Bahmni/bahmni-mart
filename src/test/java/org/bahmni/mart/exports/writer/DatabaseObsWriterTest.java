package org.bahmni.mart.exports.writer;

import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.exports.updatestrategy.ObsIncrementalUpdateStrategy;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseObsWriterTest {

    private DatabaseObsWriter databaseObsWriter;

    @Mock
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private FreeMarkerEvaluator freeMarkerEvaluatorForTableRecords;

    @Mock
    private ObsIncrementalUpdateStrategy obsIncrementalUpdater;

    @Mock
    private IncrementalStrategyContext incrementalStrategyContext;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private IncrementalUpdateConfig incrementalUpdateConfig;

    @Before
    public void setUp() throws Exception {
        databaseObsWriter = new DatabaseObsWriter();
        setValuesForMemberFields(databaseObsWriter, "formTableMetadataGenerator",
                formTableMetadataGenerator);
        setValuesForSuperClassMemberFields(databaseObsWriter, "martJdbcTemplate", martJdbcTemplate);
        setValuesForMemberFields(databaseObsWriter, "freeMarkerEvaluatorForTableRecords",
                freeMarkerEvaluatorForTableRecords);
        setValuesForMemberFields(databaseObsWriter, "incrementalStrategyContext", incrementalStrategyContext);

        databaseObsWriter.setJobDefinition(jobDefinition);
        when(jobDefinition.getType()).thenReturn("Obs");
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(incrementalUpdateConfig);
        when(incrementalStrategyContext.getStrategy(anyString())).thenReturn(obsIncrementalUpdater);
        when(incrementalUpdateConfig.getUpdateOn()).thenReturn("encounter_id");
    }

    @Test
    public void shouldCallDeleteSqlCommandOnExistingTableToRemoveVoidedRecords() {
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
        ArrayList<Obs> obsList1 = new ArrayList<>();
        Obs obs1 = new Obs(1, 2, fieldOne, "4");
        obs1.setEncounterId("56");

        Obs obs2 = new Obs(2, 2, fieldTwo, "test IT");
        obs2.setEncounterId("56");

        obsList1.add(obs1);
        obsList1.add(obs2);

        ArrayList<Obs> obsList2 = new ArrayList<>();
        Obs obs3 = new Obs(3, 2, fieldOne, "40");
        obs3.setEncounterId("560");

        Obs obs4 = new Obs(4, 2, fieldTwo, "test IT 2");
        obs4.setEncounterId("560");

        obsList2.add(obs3);
        obsList2.add(obs4);

        items.add(obsList1);
        items.add(obsList2);

        databaseObsWriter.setForm(bahmniForm);
        when(obsIncrementalUpdater.isMetaDataChanged(formName.getName())).thenReturn(false);
        when(formTableMetadataGenerator.getTableData(bahmniForm)).thenReturn(new TableData("test"));

        databaseObsWriter.write(items);


        verify(jobDefinition, atLeastOnce()).getIncrementalUpdateConfig();
        verify(incrementalUpdateConfig).getUpdateOn();
        verify(jobDefinition).getType();
        verify(incrementalStrategyContext).getStrategy("Obs");
        verify(obsIncrementalUpdater).isMetaDataChanged(formName.getName());
        HashSet<String> encounterIds = new HashSet<>(Arrays.asList("56", "560"));
        verify(obsIncrementalUpdater).deleteVoidedRecords(encounterIds,"test","encounter_id");
    }

    @Test
    public void shouldNotCallDeleteSqlCommandOnNonExistingTable() throws Exception {
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
        ArrayList<Obs> obsList1 = new ArrayList<>();
        Obs obs1 = new Obs(1, 2, fieldOne, "4");
        obs1.setEncounterId("56");

        Obs obs2 = new Obs(2, 2, fieldTwo, "test IT");
        obs2.setEncounterId("56");

        obsList1.add(obs1);
        obsList1.add(obs2);

        ArrayList<Obs> obsList2 = new ArrayList<>();
        Obs obs3 = new Obs(3, 2, fieldOne, "40");
        obs3.setEncounterId("560");

        Obs obs4 = new Obs(4, 2, fieldTwo, "test IT 2");
        obs4.setEncounterId("560");

        obsList2.add(obs3);
        obsList2.add(obs4);

        items.add(obsList1);
        items.add(obsList2);

        databaseObsWriter.setForm(bahmniForm);
        when(obsIncrementalUpdater.isMetaDataChanged(formName.getName())).thenReturn(true);
        when(formTableMetadataGenerator.getTableData(bahmniForm)).thenReturn(new TableData("test"));

        databaseObsWriter.write(items);

        verify(jobDefinition, atLeastOnce()).getIncrementalUpdateConfig();
        verify(incrementalUpdateConfig, never()).getUpdateOn();
        verify(obsIncrementalUpdater).isMetaDataChanged(formName.getName());
        verify(obsIncrementalUpdater, never()).deleteVoidedRecords(anySet(),anyString(),anyString());
        verify(jobDefinition).getType();
        verify(incrementalStrategyContext).getStrategy("Obs");
    }
}