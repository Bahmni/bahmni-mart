package org.bahmni.mart.form;

import org.bahmni.mart.exports.ObsRecordExtractorForTable;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.domain.ForeignKey;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SpecialCharacterResolver.class)
public class ObsRecordExtractorForTableTest {

    private ObsRecordExtractorForTable obsRecordExtractorForTable;

    @Before
    public void setUp() throws Exception {
        obsRecordExtractorForTable = new ObsRecordExtractorForTable("tableName");
        mockStatic(SpecialCharacterResolver.class);
    }

    @Test
    public void shouldDoNothingGivenEmptyObsListAndEmptyTableData() {
        TableData tableData = new TableData();
        List<Obs> obsList = new ArrayList<>();
        obsRecordExtractorForTable.execute(Arrays.asList(obsList), tableData);

        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(0));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataAndObsData() {
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

        when(SpecialCharacterResolver.getActualColumnName(tableData, column1)).thenReturn("first");
        when(SpecialCharacterResolver.getActualColumnName(tableData, column2)).thenReturn("second");

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
    public void shouldGiveRecordsGivenTableDataWithPrimaryAndForeignKeys() {
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

        when(SpecialCharacterResolver.getActualColumnName(tableData, column1)).thenReturn("id_tablename");
        when(SpecialCharacterResolver.getActualColumnName(tableData, column2)).thenReturn("id_second");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs1), Arrays.asList(obs2)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(2));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("id_tablename"));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(1).keySet().contains("id_second"));
        assertEquals("111", obsRecordExtractorForTable.getRecordList().get(0).get("id_tablename"));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(1).get("id_second"));
    }

    @Test
    public void shouldGiveRecordsWithParentIdOfObsForGivenTableDataWithNoPrimaryKey() {

        String primaryKeyColumnName = "id_tablename";

        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn primaryKeyColumn = new TableColumn(primaryKeyColumnName, "integer", false, null);
        tableData.setColumns(Collections.singletonList(primaryKeyColumn));

        Obs obs1 = new Obs();
        obs1.setField(new Concept(000, "some concept", 0));
        obs1.setId(111);

        Obs obs2 = new Obs();
        obs2.setField(new Concept(002, "any concept", 0));
        obs2.setId(123);

        when(SpecialCharacterResolver.getActualColumnName(tableData, primaryKeyColumn))
                .thenReturn(primaryKeyColumnName);

        obsRecordExtractorForTable.setAddMoreMultiSelectEnabledForSeparateTables(false);
        obsRecordExtractorForTable.execute(Arrays.asList(Collections.singletonList(obs1),
                Collections.singletonList(obs2)), tableData);

        List<Map<String, String>> recordList = obsRecordExtractorForTable.getRecordList();

        assertTrue(recordList.get(0).keySet().contains(primaryKeyColumnName));
        assertTrue(recordList.get(1).keySet().contains(primaryKeyColumnName));
        assertEquals("111", recordList.get(0).get(primaryKeyColumnName));
        assertEquals("123", recordList.get(1).get(primaryKeyColumnName));
    }

    @Test
    public void shouldGiveRecordsWithParentIdOfObsForGivenTableDataWithNoForeignKey() {

        TableData tableData = new TableData();
        tableData.setName("tableName");
        String foreignKeyColumnName = "id_another_tablename";
        TableColumn foreignKey = new TableColumn(foreignKeyColumnName, "integer", false, null);
        tableData.setColumns(Collections.singletonList(foreignKey));

        int parentId = 1000;

        Obs obs1 = new Obs();
        obs1.setField(new Concept(000, "tablename", 0));
        obs1.setId(111);
        obs1.setParentId(parentId);
        obs1.setParentName("another_tablename");

        Obs obs2 = new Obs();
        obs2.setField(new Concept(002, "tablename", 0));
        obs2.setId(222);
        obs2.setParentId(parentId);
        obs2.setParentName("another_tablename");

        when(SpecialCharacterResolver.getActualColumnName(tableData, foreignKey)).thenReturn(foreignKeyColumnName);

        obsRecordExtractorForTable.setAddMoreMultiSelectEnabledForSeparateTables(false);
        obsRecordExtractorForTable.execute(Arrays.asList(Collections.singletonList(obs1),
                Collections.singletonList(obs2)), tableData);

        List<Map<String, String>> recordList = obsRecordExtractorForTable.getRecordList();

        assertNotNull(recordList);
        assertThat(recordList.size(), is(2));
        recordList.forEach(record -> {
            assertTrue(record.keySet().contains(foreignKeyColumnName));
            assertEquals(parentId, (Integer.parseInt(record.get(foreignKeyColumnName))));
        });

    }

    @Test
    public void shouldGiveRecordsGivenTableDataWithMultipleForeignKeys() {
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

        when(SpecialCharacterResolver.getActualColumnName(tableData, column1)).thenReturn("id_first");
        when(SpecialCharacterResolver.getActualColumnName(tableData, column2)).thenReturn("id_second");
        when(SpecialCharacterResolver.getActualColumnName(tableData, column3)).thenReturn("id_test");

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

        when(SpecialCharacterResolver.getActualColumnName(tableData, column1)).thenReturn("id_parent");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs1, obs2)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(0).get("id_parent"));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataWithEncounterId() {
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column = new TableColumn("encounter_id", "integer", true, null);
        tableData.setColumns(Arrays.asList(column));
        Obs obs1 = new Obs();
        obs1.setField(new Concept(123, "encounter", 1));
        obs1.setEncounterId("123");

        when(SpecialCharacterResolver.getActualColumnName(tableData, column)).thenReturn("encounter_id");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs1)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("encounter_id"));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(0).get("encounter_id"));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataWithVisitIdAndPatientProgramId() {
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn visitIdColumn = new TableColumn("visit_id", "integer", true, null);
        TableColumn patientProgramIdColumn = new TableColumn("patient_program_id", "integer", true, null);
        tableData.setColumns(Arrays.asList(visitIdColumn, patientProgramIdColumn));
        Obs obs1 = new Obs();
        obs1.setField(new Concept(123, "visit", 1));
        obs1.setVisitId("123");
        obs1.setPatientProgramId("321");

        when(SpecialCharacterResolver.getActualColumnName(tableData, visitIdColumn)).thenReturn("visit_id");
        when(SpecialCharacterResolver.getActualColumnName(tableData, patientProgramIdColumn))
                .thenReturn("patient_program_id");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs1)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("visit_id"));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("patient_program_id"));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(0).get("visit_id"));
        assertEquals("321", obsRecordExtractorForTable.getRecordList().get(0).get("patient_program_id"));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataWithPatientId() {
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column = new TableColumn("patient_id", "integer", true, null);
        tableData.setColumns(Arrays.asList(column));
        Obs obs = new Obs();
        obs.setField(new Concept(123, "patient_identifier", 1));
        obs.setPatientId("123");

        when(SpecialCharacterResolver.getActualColumnName(tableData, column)).thenReturn("patient_id");

        obsRecordExtractorForTable.execute(Arrays.asList(Arrays.asList(obs)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("patient_id"));
        assertEquals("123", obsRecordExtractorForTable.getRecordList().get(0).get("patient_id"));
    }

    @Test
    public void shouldGiveRecordsWithDateModifiedAsNullWhenDateCreatedIsEqualToMaxOfTheDateCreatedOfAllObs() {
        obsRecordExtractorForTable = new ObsRecordExtractorForTable("tablename");
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column1 = new TableColumn("obs_datetime", "text", true, null);
        TableColumn column2 = new TableColumn("date_created", "text", true, null);
        TableColumn column3 = new TableColumn("date_modified", "timestamp", true, null);
        tableData.setColumns(Arrays.asList(column1, column2, column3));

        Obs obs = new Obs();
        obs.setDateCreated("2018-11-10 12:00:00");
        obs.setObsDateTime("2018-11-10 12:00:00");
        obs.setField(new Concept(121, "tableName", 1));

        when(SpecialCharacterResolver.getActualColumnName(tableData, column1)).thenReturn("obs_datetime");
        when(SpecialCharacterResolver.getActualColumnName(tableData, column2)).thenReturn("date_created");
        when(SpecialCharacterResolver.getActualColumnName(tableData, column3)).thenReturn("date_modified");
        when(SpecialCharacterResolver.getActualTableName("tablename")).thenReturn("tablename");

        obsRecordExtractorForTable.execute(Collections.singletonList(Collections.singletonList(obs)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("obs_datetime"));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("date_created"));
        assertEquals("'2018-11-10 12:00:00'", obsRecordExtractorForTable.getRecordList().get(0).get("date_created"));
        assertEquals("'2018-11-10 12:00:00'", obsRecordExtractorForTable.getRecordList().get(0).get("obs_datetime"));
        assertNull(obsRecordExtractorForTable.getRecordList().get(0).get("date_modified"));
    }

    @Test
    public void shouldGiveRecordsGivenTableDataWithDateModifiedAsMaxOfTheDateCreatedOfAllObs() {
        obsRecordExtractorForTable = new ObsRecordExtractorForTable("tablename");
        TableData tableData = new TableData();
        tableData.setName("tableName");
        TableColumn column1 = new TableColumn("obs_datetime", "text", true, null);
        TableColumn column2 = new TableColumn("date_created", "text", true, null);
        TableColumn column3 = new TableColumn("date_modified", "timestamp", true, null);
        TableColumn column4 = new TableColumn("first", "integer", true, null);
        tableData.setColumns(Arrays.asList(column1, column2, column3, column4));

        Obs obs1 = new Obs();
        obs1.setDateCreated("2018-11-10 12:00:00");
        obs1.setObsDateTime("2018-11-10 12:00:00");
        obs1.setField(new Concept(121, "tableName", 1));
        Obs obs2 = new Obs();
        obs2.setDateCreated("2018-11-10 12:01:00");
        obs2.setObsDateTime("2018-11-10 12:00:00");
        obs2.setField(new Concept(121, "first", 1));

        when(SpecialCharacterResolver.getActualColumnName(tableData, column1)).thenReturn("obs_datetime");
        when(SpecialCharacterResolver.getActualColumnName(tableData, column2)).thenReturn("date_created");
        when(SpecialCharacterResolver.getActualColumnName(tableData, column3)).thenReturn("date_modified");
        when(SpecialCharacterResolver.getActualTableName("tablename")).thenReturn("tablename");

        obsRecordExtractorForTable.execute(Collections.singletonList(Arrays.asList(obs1, obs2)), tableData);

        assertNotNull(obsRecordExtractorForTable.getRecordList());
        assertThat(obsRecordExtractorForTable.getRecordList().size(), is(1));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("obs_datetime"));
        assertTrue(obsRecordExtractorForTable.getRecordList().get(0).keySet().contains("date_created"));
        assertEquals("'2018-11-10 12:00:00'", obsRecordExtractorForTable.getRecordList().get(0).get("date_created"));
        assertEquals("'2018-11-10 12:00:00'", obsRecordExtractorForTable.getRecordList().get(0).get("obs_datetime"));
        assertEquals("'2018-11-10 12:01:00'", obsRecordExtractorForTable.getRecordList().get(0).get("date_modified"));
    }

    @Test
    public void shouldGiveRecordsWhenTableNameLengthIsMoreThan59() {

        obsRecordExtractorForTable =
                new ObsRecordExtractorForTable("mtc_additional_contributing_medical_or_treatment_related_reasons");

        String primaryKeyColumnName = "id_mtc_additional_contributing_medical_or_treatment_related";

        TableData tableData = new TableData();
        tableData.setName("mtc_additional_contributing_medical_or_treatment_related_reasons");
        TableColumn primaryKeyColumn = new TableColumn(primaryKeyColumnName, "integer", false, null);
        tableData.setColumns(Collections.singletonList(primaryKeyColumn));

        Obs obs1 = new Obs();
        obs1.setField(new Concept(000, "some concept", 0));
        obs1.setId(111);

        Obs obs2 = new Obs();
        obs2.setField(new Concept(002, "any concept", 0));
        obs2.setId(123);

        when(SpecialCharacterResolver.getActualColumnName(tableData, primaryKeyColumn))
                .thenReturn(primaryKeyColumnName);

        obsRecordExtractorForTable.setAddMoreMultiSelectEnabledForSeparateTables(false);
        obsRecordExtractorForTable.execute(Arrays.asList(Collections.singletonList(obs1),
                Collections.singletonList(obs2)), tableData);

        List<Map<String, String>> recordList = obsRecordExtractorForTable.getRecordList();

        assertTrue(recordList.get(0).keySet().contains(primaryKeyColumnName));
        assertTrue(recordList.get(1).keySet().contains(primaryKeyColumnName));
        assertEquals("111", recordList.get(0).get(primaryKeyColumnName));
        assertEquals("123", recordList.get(1).get(primaryKeyColumnName));
    }
}
