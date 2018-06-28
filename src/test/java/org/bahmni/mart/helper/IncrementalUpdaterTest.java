package org.bahmni.mart.helper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IncrementalUpdaterTest {

    private IncrementalUpdater incrementalUpdater;
    private String jobName;

    @Mock
    private JdbcTemplate openmrsJdbcTemplate;

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private MarkerMapper markerMapper;

    private String readerSql;
    private String updateOn;
    private String category;
    private String tableName;
    private String queryForEventObjects;
    private String queryForIds;

    @Before
    public void setUp() throws Exception {
        incrementalUpdater = new IncrementalUpdater();
        jobName = "JobName";
        category = "CategoryName";
        tableName = "encounter";
        readerSql = "SELECT id, name FROM test";

        setValuesForMemberFields(incrementalUpdater, "openmrsJdbcTemplate", openmrsJdbcTemplate);
        setValuesForMemberFields(incrementalUpdater, "martJdbcTemplate", martJdbcTemplate);
        setValuesForMemberFields(incrementalUpdater, "markerMapper", markerMapper);
        updateOn = "id";
        queryForEventObjects = "SELECT DISTINCT substring_index(substring_index(object, '/', -1), '?', 1) as uuid " +
                "FROM event_records WHERE id > %d AND category = '%s'";
        queryForIds = String.format("SELECT %s_id FROM %s WHERE uuid in ('%s','%s')",
                tableName, tableName,
                "2c2ca648-3fae-4719-a164-3b603b7d6d41",
                "a76f74db-9ec4-4d20-9fc9-cb449ab331a5");
    }

    @Test
    public void shouldReturnSameReaderSqlWhenThereIsNoMarkerMapForGivenJob() {
        when(markerMapper.getJobMarkerMap(jobName)).thenReturn(Optional.empty());

        String updatedReaderSql = incrementalUpdater.updateReaderSql(readerSql, jobName, updateOn);

        verify(markerMapper).getJobMarkerMap(jobName);
        assertEquals(readerSql, updatedReaderSql);
    }

    @Test
    public void shouldReturnSameReaderSqlWhenEventRecordIdForGivenJobIsZero() {
        Map markerMap = mock(Map.class);
        when(markerMap.get("event_record_id")).thenReturn(0);
        when(markerMapper.getJobMarkerMap(jobName)).thenReturn(Optional.of(markerMap));

        String actualUpdatedReaderSql = incrementalUpdater.updateReaderSql(readerSql, jobName, updateOn);

        verify(markerMapper).getJobMarkerMap(jobName);
        verify(markerMap).get("event_record_id");
        assertEquals(readerSql, actualUpdatedReaderSql);
    }

    @Test
    public void shouldQueryFromEventRecordsTableWhenEventRecordIdIsNotZeroForGivenJob() {
        Map markerMap = mock(Map.class);
        when(markerMap.get("event_record_id")).thenReturn(10);
        when(markerMap.get("category")).thenReturn(category);
        when(markerMapper.getJobMarkerMap(jobName)).thenReturn(Optional.of(markerMap));

        incrementalUpdater.updateReaderSql(readerSql, jobName, updateOn);

        verify(markerMapper).getJobMarkerMap(jobName);
        verify(markerMap, times(2)).get("event_record_id");
        verify(markerMap).get("category");
        verify(openmrsJdbcTemplate).queryForList(String.format(queryForEventObjects, 10, category), String.class);
    }

    @Test
    public void shouldQueryFromSpecificTableToFetchIdsForGivenUuids() {
        setUpForMarkerMap();
        setUpForUuids();

        incrementalUpdater.updateReaderSql(readerSql, jobName, updateOn);

        verify(openmrsJdbcTemplate).queryForList(String.format(queryForEventObjects, 10, category), String.class);
        verify(openmrsJdbcTemplate).queryForList(queryForIds, String.class);
    }


    @Test
    public void shouldUpdateReaderSqlGivenNonZeroEventRecordIdAndNonEmptyUuidsList() {
        setUpForMarkerMap();
        setUpForUuids();
        List<String> ids = Arrays.asList("1", "2");
        when(openmrsJdbcTemplate.queryForList(queryForIds, String.class)).thenReturn(ids);
        String expectedUpdatedReaderSql = String.format("SELECT * FROM ( %s ) result WHERE %s IN (1,2)", readerSql,
                updateOn);

        String updatedReaderSql = incrementalUpdater.updateReaderSql(readerSql, jobName, updateOn);

        assertEquals(expectedUpdatedReaderSql, updatedReaderSql);
    }

    @Test
    public void shouldUpdateReaderSqlWithNonExistedIdWhenUuidListIsEmpty() {
        setUpForMarkerMap();
        when(openmrsJdbcTemplate.queryForList(String.format(queryForEventObjects, 10, category), String.class))
                .thenReturn(new ArrayList<>());
        String expectedUpdatedReaderSql = String.format("SELECT * FROM ( %s ) result WHERE %s IN (-1)", readerSql,
                updateOn);

        String updatedReaderSql = incrementalUpdater.updateReaderSql(readerSql, jobName, updateOn);

        assertEquals(expectedUpdatedReaderSql, updatedReaderSql);
    }

    @Test
    public void shouldExecuteDeleteSqlForGivenIds() {
        Set<String> ids = new HashSet<>(Arrays.asList("1", "2"));
        String table = "table";
        String column = "column";

        incrementalUpdater.deleteVoidedRecords(ids, table, column);
        String sql = String.format("DELETE FROM table WHERE column IN (1,2)");

        verify(martJdbcTemplate).execute(sql);
    }

    @Test
    public void shouldNotExecuteDeleteSqlWhenIdListIsNull() {

        incrementalUpdater.deleteVoidedRecords(null, "table", "column");

        verify(martJdbcTemplate, never()).execute(anyString());
    }

    @Test
    public void shouldNotExecuteDeleteSqlWhenIdListIsEmpty() {

        incrementalUpdater.deleteVoidedRecords(Collections.emptySet(), "table", "column");

        verify(martJdbcTemplate, never()).execute(anyString());
    }

    private void setUpForMarkerMap() {
        Map markerMap = mock(Map.class);
        when(markerMap.get("event_record_id")).thenReturn(10);
        when(markerMap.get("category")).thenReturn(category);
        when(markerMap.get("table_name")).thenReturn(tableName);
        when(markerMapper.getJobMarkerMap(jobName)).thenReturn(Optional.of(markerMap));
    }

    private void setUpForUuids() {
        List<String> eventRecordObjects = new ArrayList<>();
        eventRecordObjects.add("2c2ca648-3fae-4719-a164-3b603b7d6d41");
        eventRecordObjects.add("a76f74db-9ec4-4d20-9fc9-cb449ab331a5");
        when(openmrsJdbcTemplate.queryForList(String.format(queryForEventObjects, 10, category), String.class))
                .thenReturn(eventRecordObjects);
    }
}
