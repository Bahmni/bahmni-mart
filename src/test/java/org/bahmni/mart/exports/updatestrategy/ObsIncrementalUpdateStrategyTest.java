package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.helper.MarkerManager;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

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
import static org.mockito.Matchers.eq;
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

    @Mock
    private JdbcTemplate openmrsJdbcTemplate;

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private MarkerManager markerManager;

    @Mock
    private TableDataGenerator tableDataGenerator;

    @Mock
    private TableMetadataGenerator tableMetadataGenerator;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private Map markerMap;

    @Spy
    private Map<String, Boolean> metaDataChangeMap = new HashMap<>();

    private ObsIncrementalUpdateStrategy obsIncrementalUpdater;

    private Integer maxObsId = 101010;

    private static final String MAX_EVENT_RECORD_ID = "20";
    private static final String JOB_NAME = "JobName";
    private static final String READER_SQL = "SELECT id, name FROM test";
    private static final String UPDATE_ON = "id";
    private static final String CATEGORY = "CategoryName";
    private static final String TABLE_NAME = "encounter";
    private static final String QUERY_FOR_EVENT_URLS = "SELECT DISTINCT object " +
            "FROM event_records WHERE id > %s AND id <= %s AND binary category = '%s'";
    private static final String QUERY_FOR_IDS = format("SELECT %s_id FROM %s WHERE uuid in ('%s','%s')",
            TABLE_NAME, TABLE_NAME, "99b885e9-c49c-4b5a-aa4c-e8a8fb93484d", "55527d10-5789-46e1-af16-59082938f11c");
    private static final String queryForMaxEventRecordId = "SELECT MAX(id) FROM event_records";
    private static final String UPDATED_READER_SQL = "SELECT * FROM ( %s ) result WHERE %s IN (%s) AND obs_id <=%s";
    private static final String eventRecordedForJob = "10";


    @Before
    public void setUp() throws Exception {
        obsIncrementalUpdater = new ObsIncrementalUpdateStrategy();

        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "openmrsJdbcTemplate", openmrsJdbcTemplate);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "martJdbcTemplate", martJdbcTemplate);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "markerManager", markerManager);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "tableDataGenerator", tableDataGenerator);
        obsIncrementalUpdater.setTableMetadataGenerator(tableMetadataGenerator);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "metaDataChangeMap", metaDataChangeMap);
        setValuesForSuperClassMemberFields(obsIncrementalUpdater, "maxEventRecordId",
                                            Integer.parseInt(MAX_EVENT_RECORD_ID));
        setValuesForMemberFields(obsIncrementalUpdater, "maxObsId", maxObsId);
        setValuesForMemberFields(obsIncrementalUpdater, "jobDefinitionReader", jobDefinitionReader);

        mockStatic(SpecialCharacterResolver.class);

        when(markerMap.get("event_record_id")).thenReturn(eventRecordedForJob);
        when(markerMap.get("category")).thenReturn(CATEGORY);
        when(markerMap.get("table_name")).thenReturn(TABLE_NAME);
        when(markerManager.getJobMarkerMap(JOB_NAME)).thenReturn(Optional.of(markerMap));
        when(jobDefinitionReader.getJobDefinitionByName(anyString())).thenReturn(jobDefinition);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(mock(IncrementalUpdateConfig.class));
    }

    @Test
    public void shouldReturnSameReaderSqlWhenThereIsNoMarkerMapForGivenJob() {
        when(markerManager.getJobMarkerMap(JOB_NAME)).thenReturn(Optional.empty());

        String updatedReaderSql = obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        verify(markerManager).getJobMarkerMap(JOB_NAME);
        assertEquals(format("%s AND obs0.obs_id <=%d", READER_SQL, maxObsId), updatedReaderSql);
    }

    @Test
    public void shouldReturnSameReaderSqlWhenEventRecordIdForGivenJobIsZero() {
        Map markerMap = mock(Map.class);
        when(markerMap.get("event_record_id")).thenReturn(0);
        when(markerManager.getJobMarkerMap(JOB_NAME)).thenReturn(Optional.of(markerMap));

        String actualUpdatedReaderSql = obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        verify(markerManager).getJobMarkerMap(JOB_NAME);
        verify(markerMap).get("event_record_id");
        assertEquals(String.format("%s AND obs0.obs_id <=%s", READER_SQL, maxObsId), actualUpdatedReaderSql);
    }

    @Test
    public void shouldUpdateReaderSqlWithNonExistIdIfJoinedIdsEmpty() {
        String queryForEventRecordObjectUrls = format(QUERY_FOR_EVENT_URLS, eventRecordedForJob,
                MAX_EVENT_RECORD_ID, CATEGORY);
        String wrongUuid = "99b885e9-c49c-4b5a-aa4c-e8a8fb93484d";
        when(openmrsJdbcTemplate.queryForList(queryForEventRecordObjectUrls, String.class))
                .thenReturn(Arrays.asList(wrongUuid));
        String queryForIDs = format("SELECT %s_id FROM %s WHERE uuid in ('%s')",
                                TABLE_NAME, TABLE_NAME, wrongUuid);
        when(openmrsJdbcTemplate.queryForList(queryForIDs, String.class)).thenReturn(Arrays.asList(""));
        String expectedUpdatedReaderSql = format(UPDATED_READER_SQL, READER_SQL, UPDATE_ON, "-1", maxObsId);

        String actualUpdateReaderSql = obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        verify(markerManager).getJobMarkerMap(JOB_NAME);
        verify(markerMap, times(2)).get("event_record_id");
        verify(markerMap).get("category");
        verify(openmrsJdbcTemplate).queryForList(format(QUERY_FOR_EVENT_URLS, eventRecordedForJob,
                MAX_EVENT_RECORD_ID, CATEGORY),
                String.class);
        verify(openmrsJdbcTemplate).queryForList(queryForIDs,
                String.class);
        assertEquals(expectedUpdatedReaderSql, actualUpdateReaderSql);
    }

    @Test
    public void shouldQueryFromEventRecordsTableWhenEventRecordIdIsNotZeroForGivenJob() {

        String readerSql = obsIncrementalUpdater.updateReaderSql(this.READER_SQL, JOB_NAME, UPDATE_ON);

        assertEquals(String.format("SELECT * FROM ( SELECT id, name FROM test ) result " +
                "WHERE id IN (-1) AND obs_id <=%d", maxObsId), readerSql);
        verify(markerManager).getJobMarkerMap(JOB_NAME);
        verify(markerMap, times(2)).get("event_record_id");
        verify(markerMap).get("category");
        verify(openmrsJdbcTemplate).queryForList(
                format(QUERY_FOR_EVENT_URLS, eventRecordedForJob, MAX_EVENT_RECORD_ID, CATEGORY), String.class);
    }

    @Test
    public void shouldQueryFromSpecificTableToFetchIdsForGivenUuids() {
        setUpForUuids();

        obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        verify(openmrsJdbcTemplate).queryForList(
                format(QUERY_FOR_EVENT_URLS, eventRecordedForJob, MAX_EVENT_RECORD_ID, CATEGORY), String.class);
        verify(openmrsJdbcTemplate).queryForList(QUERY_FOR_IDS, String.class);
    }


    @Test
    public void shouldUpdateReaderSqlGivenNonZeroEventRecordIdAndNonEmptyUuidsList() {
        setUpForUuids();
        List<String> ids = Arrays.asList("1", "2");
        when(openmrsJdbcTemplate.queryForList(QUERY_FOR_IDS, String.class)).thenReturn(ids);
        String expectedReaderSql = format("SELECT * FROM ( %s ) result " +
                "WHERE %s IN (1,2) AND obs_id <=%s", READER_SQL, UPDATE_ON, maxObsId);

        String updatedReaderSql = obsIncrementalUpdater.updateReaderSql(READER_SQL, JOB_NAME, UPDATE_ON);

        assertEquals(expectedReaderSql, updatedReaderSql);
    }

    @Test
    public void shouldUpdateReaderSqlWithNonExistedIdWhenUuidListIsEmpty() {
        when(openmrsJdbcTemplate.queryForList(
                format(QUERY_FOR_EVENT_URLS, eventRecordedForJob, MAX_EVENT_RECORD_ID, CATEGORY), String.class))
                .thenReturn(new ArrayList<>());
        String expectedUpdatedReaderSql = format("SELECT * FROM ( %s ) result WHERE %s IN (-1) AND obs_id <=%s",
                READER_SQL, UPDATE_ON, maxObsId);

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
        setValuesForSuperClassMemberFields(obsIncrementalUpdater,
                "maxEventRecordId", 145678);

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
        when(tableMetadataGenerator.getTableDataByName(actualTableName))
                .thenReturn(tableData);

        boolean metaDataChanged = obsIncrementalUpdater.isMetaDataChanged(formName, JOB_NAME);

        assertTrue(metaDataChanged);
        verifyStatic();
        getUpdatedTableNameIfExist(actualTableName);

        verify(tableDataGenerator).getTableDataFromMart(updatedTableName, "SELECT * FROM form_name_one");
        verifyStatic();
        SpecialCharacterResolver.getActualTableName(actualTableName);
        verifyStatic();
        SpecialCharacterResolver.resolveTableData(tableData);
        verify(tableMetadataGenerator).getTableDataByName(actualTableName);
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
        when(tableMetadataGenerator.getTableDataByName(actualTableName))
                .thenReturn(tableData);

        boolean metaDataChanged = obsIncrementalUpdater.isMetaDataChanged(formName, JOB_NAME);

        assertFalse(metaDataChanged);
        verifyStatic();
        getUpdatedTableNameIfExist(actualTableName);
        verifyStatic();
        SpecialCharacterResolver.getActualTableName(actualTableName);
        verifyStatic();
        SpecialCharacterResolver.resolveTableData(tableData);

        verify(tableDataGenerator).getTableDataFromMart(updatedTableName, "SELECT * FROM form_name_one");
        verify(tableMetadataGenerator).getTableDataByName(actualTableName);
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
        verify(tableMetadataGenerator, never()).getTableDataByName(actualTableName);
    }

    @Test
    public void shouldNotThrowBadSalGrammarExceptionWhenTheTableIsNotPresent() {
        String formName = "table, name";
        String actualTableName = "table,_name";
        when(SpecialCharacterResolver.getActualTableName(anyString())).thenReturn(actualTableName);
        when(tableMetadataGenerator.getTableDataByName(actualTableName)).thenReturn(new TableData(actualTableName));
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

    private void setUpForUuids() {
        List<String> eventRecordObjects = Arrays.asList(
                "/openmrs/ws/rest/v1/appointment?uuid=99b885e9-c49c-4b5a-aa4c-e8a8fb93484d",
                "/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/55527d10-5789-46e1-af16-59082938f11c?includeAll=true");
        when(openmrsJdbcTemplate.queryForList(
                format(QUERY_FOR_EVENT_URLS, eventRecordedForJob, MAX_EVENT_RECORD_ID, CATEGORY), String.class))
                .thenReturn(eventRecordObjects);
    }

    @Test
    public void shouldReturnFullLoadSqlWhenTableMetadataIsChanged() {
        ObsIncrementalUpdateStrategy obsIncrementalUpdateStrategySpy = Mockito.spy(ObsIncrementalUpdateStrategy.class);
        Mockito.doReturn(Boolean.TRUE).when(obsIncrementalUpdateStrategySpy)
                .isMetaDataChanged(anyString(), anyString());
        ReflectionTestUtils.setField(obsIncrementalUpdateStrategySpy, "maxObsId", 101010);

        String updateReaderSql = obsIncrementalUpdateStrategySpy
                .updateReaderSql("some sql", "obs job", "encounter_id", "table name");

        assertEquals(String.format("some sql AND obs0.obs_id <=%d", maxObsId), updateReaderSql);

    }

    @Test
    public void shouldReturnIncrementalLoadSqlWhenTableMetadataIsNotChanged() {
        ObsIncrementalUpdateStrategy obsIncrementalUpdateStrategySpy = Mockito.spy(ObsIncrementalUpdateStrategy.class);
        ReflectionTestUtils.setField(obsIncrementalUpdateStrategySpy, "maxObsId", 101010);
        Mockito.doReturn(Boolean.FALSE).when(obsIncrementalUpdateStrategySpy)
                .isMetaDataChanged(anyString(), anyString());
        Mockito.doReturn(String.format("some sql AND obs_id <=%d", maxObsId))
                .when(obsIncrementalUpdateStrategySpy).updateReaderSql(anyString(), anyString(), anyString());

        String updateReaderSql = obsIncrementalUpdateStrategySpy
                .updateReaderSql("some sql", "obs job", "encounter_id", "table name");

        verify(obsIncrementalUpdateStrategySpy).updateReaderSql("some sql", "obs job", "encounter_id");
        assertEquals(String.format("some sql AND obs_id <=%d", maxObsId), updateReaderSql);
    }

    @Test
    public void shouldReturnTrueWhenJobDefinitionIsNull() throws Exception {
        Mockito.doReturn(null).when(jobDefinitionReader).getJobDefinitionByName("jobName");

        assertTrue(obsIncrementalUpdater.getMetaDataChangeStatus("table_name","jobName"));
    }

    @Test
    public void shouldReturnTrueWhenIncrementalUpdateConfigurationIsNull() throws Exception {
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(null);

        assertTrue(obsIncrementalUpdater.getMetaDataChangeStatus("table_name","jobName"));
    }


    @Test
    public void shouldReturnFalseWhenThereIsNoMetadataChange() throws Exception {
        String tableName = "table_name";
        String jobName = "Orders Data";
        TableData tableData = new TableData(tableName);
        when(tableMetadataGenerator.getTableDataByName(tableName)).thenReturn(tableData);
        when(tableDataGenerator.getTableDataFromMart(eq(tableName), anyString())).thenReturn(tableData);
        when(SpecialCharacterResolver.getActualTableName(anyString())).thenReturn(tableName);
        when(SpecialCharacterResolver.getUpdatedTableNameIfExist(anyString())).thenReturn(tableName);


        boolean metaDataChangeStatus = obsIncrementalUpdater.getMetaDataChangeStatus(tableName, jobName);

        verify(tableMetadataGenerator).getTableDataByName(tableName);
        verify(tableDataGenerator).getTableDataFromMart(eq(tableName), anyString());
        assertFalse(metaDataChangeStatus);
        verifyStatic();
        SpecialCharacterResolver.resolveTableData(tableData);
    }

    @Test
    public void shouldReturnTrueWhenThereIsAMetadataChange() throws Exception {
        String tableName = "table_name";
        String jobName = "Orders Data";
        TableData tableData = new TableData(tableName);
        when(tableDataGenerator.getTableDataFromMart(eq(tableName), anyString())).thenReturn(new TableData());
        when(tableMetadataGenerator.getTableDataByName(tableName)).thenReturn(tableData);
        when(SpecialCharacterResolver.getActualTableName(anyString())).thenReturn(tableName);
        when(SpecialCharacterResolver.getUpdatedTableNameIfExist(anyString())).thenReturn(tableName);

        boolean metaDataChangeStatus = obsIncrementalUpdater.getMetaDataChangeStatus(tableName, jobName);

        verify(tableMetadataGenerator).getTableDataByName(tableName);
        verify(tableDataGenerator).getTableDataFromMart(eq(tableName), anyString());
        assertTrue(metaDataChangeStatus);
        verifyStatic();
        SpecialCharacterResolver.resolveTableData(tableData);
    }

}
