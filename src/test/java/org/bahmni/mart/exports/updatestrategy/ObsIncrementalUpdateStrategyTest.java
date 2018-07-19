package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.helper.MarkerManager;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.bahmni.mart.table.FormTableMetadataGenerator.getProcessedName;
import static org.bahmni.mart.table.SpecialCharacterResolver.getUpdatedTableNameIfExist;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest(SpecialCharacterResolver.class)
@RunWith(PowerMockRunner.class)
public class ObsIncrementalUpdateStrategyTest {

    private ObsIncrementalUpdateStrategy obsIncrementalUpdater;
    private static final String JOB_NAME = "JobName";

    @Mock
    private JdbcTemplate openmrsJdbcTemplate;

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private MarkerManager markerManager;

    @Mock
    private TableDataGenerator tableDataGenerator;

    @Mock
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Spy
    private Map<String, Boolean> metaDataChangeMap = new HashMap<>();

    private static final String READER_SQL = "SELECT id, name FROM test";
    private static final String UPDATE_ON = "id";
    private static final String CATEGORY = "CategoryName";
    private static final String TABLE_NAME = "encounter";
    private static final String QUERY_FOR_EVENT_URLS = "SELECT DISTINCT object " +
            "FROM event_records WHERE id > %s AND id <= %s AND binary category = '%s'";
    private static final String QUERY_FOR_IDS = format("SELECT %s_id FROM %s WHERE uuid in ('%s','%s')",
            TABLE_NAME, TABLE_NAME, "99b885e9-c49c-4b5a-aa4c-e8a8fb93484d", "55527d10-5789-46e1-af16-59082938f11c");
    private static final String queryForMaxEventRecordId = "SELECT MAX(id) FROM event_records";

    @Before
    public void setUp() throws Exception {
        obsIncrementalUpdater = new ObsIncrementalUpdateStrategy();

        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "openmrsJdbcTemplate", openmrsJdbcTemplate);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "martJdbcTemplate", martJdbcTemplate);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "markerManager", markerManager);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "tableDataGenerator", tableDataGenerator);
        setValuesForMemberFields(obsIncrementalUpdater, "formTableMetadataGenerator", formTableMetadataGenerator);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "metaDataChangeMap", metaDataChangeMap);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "maxEventRecordId", 20);

        mockStatic(SpecialCharacterResolver.class);
    }

    @Test
    public void shouldReturnSameReaderSqlWhenThereIsNoMarkerMapForGivenJob() {
        when(markerManager.getJobMarkerMap(JOB_NAME)).thenReturn(Optional.empty());

        String updatedReaderSql = obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        verify(markerManager).getJobMarkerMap(JOB_NAME);
        assertEquals(READER_SQL, updatedReaderSql);
    }

    @Test
    public void shouldReturnSameReaderSqlWhenEventRecordIdForGivenJobIsZero() {
        Map markerMap = mock(Map.class);
        when(markerMap.get("event_record_id")).thenReturn(0);
        when(markerManager.getJobMarkerMap(JOB_NAME)).thenReturn(Optional.of(markerMap));

        String actualUpdatedReaderSql = obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        verify(markerManager).getJobMarkerMap(JOB_NAME);
        verify(markerMap).get("event_record_id");
        assertEquals(READER_SQL, actualUpdatedReaderSql);
    }

    @Test
    public void shouldQueryFromEventRecordsTableWhenEventRecordIdIsNotZeroForGivenJob() throws Exception {
        Map markerMap = mock(Map.class);
        when(markerMap.get("event_record_id")).thenReturn("10");
        when(markerMap.get("category")).thenReturn(CATEGORY);
        when(markerManager.getJobMarkerMap(JOB_NAME)).thenReturn(Optional.of(markerMap));

        String readerSql = obsIncrementalUpdater.updateReaderSql(this.READER_SQL, JOB_NAME, UPDATE_ON);

        assertEquals("SELECT * FROM ( SELECT id, name FROM test ) result WHERE id IN (-1)", readerSql);
        verify(markerManager).getJobMarkerMap(JOB_NAME);
        verify(markerMap, times(2)).get("event_record_id");
        verify(markerMap).get("category");
        verify(openmrsJdbcTemplate).queryForList(format(QUERY_FOR_EVENT_URLS, "10", "20", CATEGORY),
                String.class);
    }

    @Test
    public void shouldQueryFromSpecificTableToFetchIdsForGivenUuids() {
        setUpForMarkerMap();
        setUpForUuids();

        obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        verify(openmrsJdbcTemplate).queryForList(format(QUERY_FOR_EVENT_URLS, "10", "20", CATEGORY), String.class);
        verify(openmrsJdbcTemplate).queryForList(QUERY_FOR_IDS, String.class);
    }


    @Test
    public void shouldUpdateReaderSqlGivenNonZeroEventRecordIdAndNonEmptyUuidsList() {
        setUpForMarkerMap();
        setUpForUuids();
        List<String> ids = Arrays.asList("1", "2");
        when(openmrsJdbcTemplate.queryForList(QUERY_FOR_IDS, String.class)).thenReturn(ids);
        String expectedReaderSql = format("SELECT * FROM ( %s ) result WHERE %s IN (1,2)", READER_SQL, UPDATE_ON);

        String updatedReaderSql = obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        assertEquals(expectedReaderSql, updatedReaderSql);
    }

    @Test
    public void shouldUpdateReaderSqlWithNonExistedIdWhenUuidListIsEmpty() {
        setUpForMarkerMap();
        when(openmrsJdbcTemplate.queryForList(format(QUERY_FOR_EVENT_URLS, "10", "20", CATEGORY), String.class))
                .thenReturn(new ArrayList<>());
        String expectedUpdatedReaderSql = format("SELECT * FROM ( %s ) result WHERE %s IN (-1)", READER_SQL,
                UPDATE_ON);

        String updatedReaderSql = obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        assertEquals(expectedUpdatedReaderSql, updatedReaderSql);
    }

    @Test
    public void shouldExecuteDeleteSqlForGivenIds() {
        Set<String> ids = new HashSet<>(Arrays.asList("1", "2"));
        String table = "table";
        String column = "column";

        obsIncrementalUpdater.deleteVoidedRecords(ids, table, column);

        verify(martJdbcTemplate).execute("DELETE FROM table WHERE column IN (1,2)");
    }

    @Test
    public void shouldNotExecuteDeleteSqlWhenIdListIsNull() {

        obsIncrementalUpdater.deleteVoidedRecords(null, "table", "column");

        verify(martJdbcTemplate, never()).execute(anyString());
    }

    @Test
    public void shouldNotExecuteDeleteSqlWhenIdListIsEmpty() {

        obsIncrementalUpdater.deleteVoidedRecords(Collections.emptySet(), "table", "column");

        verify(martJdbcTemplate, never()).execute(anyString());
    }

    @Test
    public void shouldCallUpdateMarkerWithMaxEventRecordIdForGivenJobName() throws Exception {
        String jobName = "obs";
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "maxEventRecordId", 145678);

        obsIncrementalUpdater.updateMarker(jobName);

        verify(markerManager).updateMarker(jobName, 145678);
    }

    @Test
    public void shouldCallUpdateMarkerWithZeroWhenMaxMarkerIdIsNull() throws Exception {
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "maxEventRecordId", null);

        String jobName = "obs";

        obsIncrementalUpdater.updateMarker(jobName);

        verify(markerManager).updateMarker(jobName, 0);
    }

    @Test
    public void shouldNotQueryForMaxEventRecordIdWhenTheSameFieldIsNotNull() throws Exception {
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "maxEventRecordId", 123);

        obsIncrementalUpdater.updateMarker("JOB_NAME");

        verify(openmrsJdbcTemplate, never()).queryForObject(queryForMaxEventRecordId, String.class);
    }

    @Test
    public void shouldReturnTrueWhenThereIsMetaDataChange() {
        String formName = "form, name@one";
        String actualTableName = "form,_name@one";
        TableData tableData = new TableData(actualTableName);
        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("id_form_name_one", "integer", true, null));
        tableColumns.add(new TableColumn("field_1", "int", false, null));
        tableData.setColumns(tableColumns);

        String updatedTableName = "form_name_one";
        when(SpecialCharacterResolver.getActualTableName(anyString())).thenReturn(actualTableName);
        when(getUpdatedTableNameIfExist(actualTableName)).thenReturn(updatedTableName);
        when(tableDataGenerator.getTableDataFromMart(updatedTableName, "SELECT * FROM form_name_one"))
                .thenReturn(new TableData());
        when(formTableMetadataGenerator.getTableDataByName(actualTableName))
                .thenReturn(tableData);

        boolean metaDataChanged = obsIncrementalUpdater.isMetaDataChanged(formName, JOB_NAME);

        assertTrue(metaDataChanged);
        verifyStatic();
        getUpdatedTableNameIfExist(actualTableName);

        verify(tableDataGenerator).getTableDataFromMart(updatedTableName, "SELECT * FROM form_name_one");
        verifyStatic();
        SpecialCharacterResolver.getActualTableName(actualTableName);
        verify(formTableMetadataGenerator).getTableDataByName(actualTableName);
        assertFalse(metaDataChangeMap.isEmpty());
        verify(metaDataChangeMap).put("form,_name@one", true);
    }

    @Test
    public void shouldReturnFalseWhenThereIsNoMetaDataChange() {
        String formName = "form, name@one";
        String actualTableName = "form,_name@one";
        TableData tableData = new TableData(actualTableName);
        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("id_form_name_one", "integer", true, null));
        tableColumns.add(new TableColumn("field_1", "int", false, null));
        tableData.setColumns(tableColumns);

        String updatedTableName = "form_name_one";
        when(getUpdatedTableNameIfExist(actualTableName)).thenReturn(updatedTableName);
        when(SpecialCharacterResolver.getActualTableName(anyString())).thenReturn(actualTableName);
        when(tableDataGenerator.getTableDataFromMart(updatedTableName, "SELECT * FROM form_name_one"))
                .thenReturn(tableData);
        when(formTableMetadataGenerator.getTableDataByName(actualTableName))
                .thenReturn(tableData);

        boolean metaDataChanged = obsIncrementalUpdater.isMetaDataChanged(formName, JOB_NAME);

        assertFalse(metaDataChanged);
        verifyStatic();
        getUpdatedTableNameIfExist(actualTableName);
        verifyStatic();
        SpecialCharacterResolver.getActualTableName(actualTableName);

        verify(tableDataGenerator).getTableDataFromMart(updatedTableName, "SELECT * FROM form_name_one");
        verify(formTableMetadataGenerator).getTableDataByName(actualTableName);
        assertFalse(metaDataChangeMap.isEmpty());
        verify(metaDataChangeMap).put("form,_name@one", false);
    }

    @Test
    public void shouldReturnFromMetaDataChangeMapWhenTheEntryIsStoredInMap() {
        String formName = "form, name@one";
        String actualTableName = getProcessedName(formName);
        metaDataChangeMap.put(actualTableName, true);

        assertTrue(obsIncrementalUpdater.isMetaDataChanged(formName, JOB_NAME));

        verify(metaDataChangeMap).get(actualTableName);
        verifyStatic(never());
        getUpdatedTableNameIfExist(actualTableName);

        verify(tableDataGenerator, never()).getTableDataFromMart(anyString(), anyString());
        verify(formTableMetadataGenerator, never()).getTableDataByName(actualTableName);
    }

    @Test
    public void shouldNotThrowBadSalGrammarExceptionWhenTheTableIsNotPresent() {
        String formName = "table, name";
        String actualTableName = "table,_name";
        when(SpecialCharacterResolver.getActualTableName(anyString())).thenReturn(actualTableName);
        when(formTableMetadataGenerator.getTableDataByName(actualTableName)).thenReturn(new TableData(actualTableName));
        when(getUpdatedTableNameIfExist(actualTableName)).thenReturn("table_name");
        when(tableDataGenerator.getTableDataFromMart("table_name", "SELECT * FROM table_name"))
                .thenThrow(BadSqlGrammarException.class);

        boolean metaDataChanged = obsIncrementalUpdater.isMetaDataChanged(formName, JOB_NAME);

        assertTrue(metaDataChanged);
        verifyStatic();
        getUpdatedTableNameIfExist(actualTableName);
        verifyStatic();
        SpecialCharacterResolver.getActualTableName(actualTableName);
        verify(tableDataGenerator).getTableDataFromMart("table_name", "SELECT * FROM table_name");
    }

    private void setUpForMarkerMap() {
        Map markerMap = mock(Map.class);
        when(markerMap.get("event_record_id")).thenReturn("10");
        when(markerMap.get("category")).thenReturn(CATEGORY);
        when(markerMap.get("table_name")).thenReturn(TABLE_NAME);
        when(markerManager.getJobMarkerMap(JOB_NAME)).thenReturn(Optional.of(markerMap));
    }

    private void setUpForUuids() {
        List<String> eventRecordObjects = Arrays.asList(
                "/openmrs/ws/rest/v1/appointment?uuid=99b885e9-c49c-4b5a-aa4c-e8a8fb93484d",
                "/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/55527d10-5789-46e1-af16-59082938f11c?includeAll=true");
        when(openmrsJdbcTemplate.queryForList(format(QUERY_FOR_EVENT_URLS, "10", "20", CATEGORY), String.class))
                .thenReturn(eventRecordObjects);
    }
}
