package org.bahmni.mart.form;

import org.bahmni.mart.exports.ObsRecordExtractorForTable;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.table.domain.ForeignKey;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
public class ObsRecordExtractorForTableTest {

    private ObsRecordExtractorForTable obsRecordExtractorForTable;

    @Before
    public void setUp() throws Exception {
        obsRecordExtractorForTable = new ObsRecordExtractorForTable("tableName");
    }

    @Test
    public void shouldDoNothingGivenEmptyObsListAndEmptyTableData() throws Exception {
        TableData tableData = new TableData();
        List<Obs> obsList = new ArrayList<>();
        obsRecordExtractorForTable.execute(Arrays.asList(obsList), tableData);

        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(0));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataAndObsData() throws Exception {
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column1 = new TableColumn("first", "integer", true, null);
        TableColumn column2 = new TableColumn("second", "integer", false, null);
        tableData.setColumns(Arrays.asList(column1, column2));
        Obs obs1 = new Obs();
        obs1.setValue("value1");
        obs1.setField(new Concept(0, "first", 0));
        Obs obs2 = new Obs();
        obs2.setValue("value2");
        obs2.setField(new Concept(111, "second", 0));
        List<Obs> obsList = Arrays.asList(obs1, obs2);

        obsRecordExtractorForTable.execute(Arrays.asList(obsList), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertThat(obsRecordExtractorForTable.getRecordList().get(0).size(), is(2));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("first"));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("second"));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).values().contains("value1"));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).values().contains("value2"));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataWithPrimaryAndForeignKeys() throws Exception {
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column1 = new TableColumn("id_tablename", "integer", true, null);
        TableColumn column2 = new TableColumn("id_second", "integer", false,
                new ForeignKey("id_second", "parent"));
        tableData.setColumns(Arrays.asList(column1, column2));
        Obs obs1 = new Obs();
        obs1.setField(new Concept(000, "tablename", 0));
        obs1.setId(111);
        Obs obs2 = new Obs();
        obs2.setField(new Concept(111, "second", 0));
        obs2.setParentId(123);
        obs2.setId(222);
        obs2.setParentName("second");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs1), Arrays.asList(obs2)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(2));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("id_tablename"));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(1).keySet().contains("id_second"));
        assertEquals("111", obsRecordExtractorForTable.getRecordList().get(0).get("id_tablename"));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(1).get("id_second"));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataWithMultipleForeignKeys() throws Exception {
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column1 = new TableColumn("id_first", "integer", true,
                new ForeignKey("id_first", "parent1"));
        TableColumn column2 = new TableColumn("id_second", "integer", false,
                new ForeignKey("id_second", "parent2"));
        TableColumn column3 = new TableColumn("id_test", "integer", false, null);

        tableData.setColumns(Arrays.asList(column1, column2, column3));
        Obs obs1 = new Obs();
        obs1.setField(new Concept(111, "first", 0));
        obs1.setParentId(321);
        obs1.setId(111);
        obs1.setParentName("first");
        Obs obs2 = new Obs();
        obs2.setField(new Concept(222, "second", 0));
        obs2.setParentId(123);
        obs2.setId(222);
        obs2.setParentName("second");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs1), Arrays.asList(obs2)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(2));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("id_first"));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(1).keySet().contains("id_second"));
        assertEquals("321", obsRecordExtractorForTable.getRecordList().get(0).get("id_first"));
        assertNull(obsRecordExtractorForTable.getRecordList().get(0).get("id_test"));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(1).get("id_second"));
        assertNull(obsRecordExtractorForTable.getRecordList().get(1).get("id_test"));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataAndBothFormLevelAndLeafObs() {
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column1 = new TableColumn("id_parent", "integer", false,
                new ForeignKey("id_parent", "parent1"));
        tableData.setColumns(Arrays.asList(column1));

        Obs obs1 = new Obs();
        obs1.setEncounterId("121");
        obs1.setId(122);
        obs1.setParentId(123);
        obs1.setField(new Concept(222, "second", 0));
        obs1.setParentName("super parent");

        Obs obs2 = new Obs();
        obs2.setEncounterId("121");
        obs2.setId(122);
        obs2.setParentId(123);
        obs2.setField(new Concept(222, "second", 0));
        obs2.setParentName("parent");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs1, obs2)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(0).get("id_parent"));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataWithEncounterId() throws Exception {
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column = new TableColumn("encounter_id", "integer", true, null);
        tableData.setColumns(Arrays.asList(column));
        Obs obs1 = new Obs();
        obs1.setField(new Concept(123, "encounter", 1));
        obs1.setEncounterId("123");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs1)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("encounter_id"));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(0).get("encounter_id"));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataWithPatientId() throws Exception {
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column = new TableColumn("patient_id", "integer", true, null);
        tableData.setColumns(Arrays.asList(column));
        Obs obs = new Obs();
        obs.setField(new Concept(123, "patient_identifier", 1));
        obs.setPatientId("123");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("patient_id"));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(0).get("patient_id"));
    }
}
