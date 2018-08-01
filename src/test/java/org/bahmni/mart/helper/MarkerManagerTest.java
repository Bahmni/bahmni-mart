package org.bahmni.mart.helper;

import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.bahmni.mart.CommonTestHelper.getPrivateMethod;
import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MarkerManagerTest {

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private JdbcTemplate openmrsJdbcTemplate;

    @Mock
    private JobDefinition existingJobDefinition;

    @Mock
    private IncrementalUpdateConfig incrementalUpdateConfigForExist;

    @Mock
    private IncrementalUpdateConfig incrementalUpdateConfigForNonExist;

    @Mock
    private JobDefinition nonExistsJobDefinition;

    private MarkerManager markerManager;

    @Mock
    private Logger logger;

    private static final String MARKERS_QUERY = "SELECT * FROM markers";
    private Map<String, Object> obsMarkerMap;
    private List<Map<String, Object>> markerMapList;

    @Before
    public void setUp() throws Exception {
        markerManager = new MarkerManager();
        setValuesForMemberFields(markerManager, "martJdbcTemplate", martJdbcTemplate);
        setValuesForMemberFields(markerManager, "openmrsJdbcTemplate", openmrsJdbcTemplate);
        setValueForFinalStaticField(MarkerManager.class, "logger", logger);

        markerMapList = new ArrayList<>();
        obsMarkerMap = new HashMap<>();
        obsMarkerMap.put("job_name", "Obs");
        markerMapList.add(obsMarkerMap);

        when(martJdbcTemplate.queryForList(MARKERS_QUERY)).thenReturn(markerMapList);
        Method setValidEventCategories = getPrivateMethod(markerManager, "setValidEventCategories");
        setValidEventCategories.invoke(markerManager);
    }

    @Test
    public void shouldQueryForMarkersWhenMarkerMapListIsNull() {
        Optional<Map<String, Object>> actualObsMarkerMap = markerManager.getJobMarkerMap("Obs");

        verify(martJdbcTemplate).queryForList(MARKERS_QUERY);
        verify(openmrsJdbcTemplate).queryForList(anyString());
        assertTrue(actualObsMarkerMap.isPresent());
        assertEquals(obsMarkerMap, actualObsMarkerMap.get());
    }

    @Test
    public void shouldNotQueryForMarkersWhenMarkerMapListIsNotNull() throws Exception {
        setValuesForMemberFields(markerManager, "markerMapList", markerMapList);

        Optional<Map<String, Object>> actualObsMarkerMap = markerManager.getJobMarkerMap("Obs");

        verify(openmrsJdbcTemplate).queryForList(anyString());
        verify(martJdbcTemplate, times(0)).queryForList(MARKERS_QUERY);
        assertTrue(actualObsMarkerMap.isPresent());
        assertEquals(obsMarkerMap, actualObsMarkerMap.get());
    }

    @Test
    public void shouldReturnEmptyOptionalWhenMarkerTableIsNotPresent() {
        String markersQuery = "SELECT * FROM markers";
        when(martJdbcTemplate.queryForList(markersQuery)).thenThrow(BadSqlGrammarException.class);

        Optional<Map<String, Object>> actualMarkerMap = markerManager.getJobMarkerMap("Obs");

        verify(martJdbcTemplate).queryForList(markersQuery);
        assertEquals(Optional.empty(), actualMarkerMap);
    }

    @Test
    public void shouldUpdateMarkerTableWithGivenEventRecordIdForGivenJob() {
        markerManager.updateMarker("obs", 123);

        verify(martJdbcTemplate).execute("UPDATE markers SET event_record_id = 123 WHERE job_name = 'obs'");
    }

    @Test
    public void shouldLogErrorWhenMarkersTableIsNotPresent() {
        doThrow(BadSqlGrammarException.class).when(martJdbcTemplate).execute(anyString());

        markerManager.updateMarker("obs", 123);

        verify(logger).error("Failed to update event_record_id for obs, markers table is not present");
    }

    @Test
    public void shouldInsertMarkerRecordsForGivenJobDefinitions() throws Exception {
        setValuesForMemberFields(markerManager, "validEventCategories",
                new HashSet<>(Collections.singletonList("category")));
        when(existingJobDefinition.getIncrementalUpdateConfig()).thenReturn(incrementalUpdateConfigForExist);
        when(existingJobDefinition.getName()).thenReturn("Obs");
        when(nonExistsJobDefinition.getIncrementalUpdateConfig()).thenReturn(incrementalUpdateConfigForNonExist);
        String nonExistJobName = "non exist job";
        when(nonExistsJobDefinition.getName()).thenReturn(nonExistJobName);
        String category = "category";
        when(incrementalUpdateConfigForNonExist.getEventCategory()).thenReturn(category);
        String tableName = "tableName";
        when(incrementalUpdateConfigForNonExist.getOpenmrsTableName()).thenReturn(tableName);

        markerManager.insertMarkers(Arrays.asList(existingJobDefinition, nonExistsJobDefinition));

        verify(martJdbcTemplate, times(1)).queryForList(MARKERS_QUERY);
        verify(existingJobDefinition).getName();
        verify(existingJobDefinition).getIncrementalUpdateConfig();
        verify(incrementalUpdateConfigForNonExist).getEventCategory();
        verify(incrementalUpdateConfigForNonExist).getOpenmrsTableName();
        verify(nonExistsJobDefinition, atLeastOnce()).getName();
        verify(nonExistsJobDefinition, atLeastOnce()).getIncrementalUpdateConfig();
        verify(martJdbcTemplate).execute(String.format("INSERT INTO markers (job_name, event_record_id," +
                " category, table_name) VALUES ('%s', 0, '%s', '%s');", nonExistJobName, category, tableName));
        verify(logger, never()).warn(anyString());
    }

    @Test
    public void shouldLogWarningMessageIfTheGivenCategoryIsNotPresentInOpeners() throws Exception {
        when(nonExistsJobDefinition.getIncrementalUpdateConfig()).thenReturn(incrementalUpdateConfigForNonExist);
        String nonExistJobName = "non exist";
        when(nonExistsJobDefinition.getName()).thenReturn(nonExistJobName);
        when(incrementalUpdateConfigForNonExist.getEventCategory()).thenReturn("wrongCategory");

        markerManager.insertMarkers(Collections.singletonList(nonExistsJobDefinition));
        verify(openmrsJdbcTemplate).queryForList(anyString());
        verify(logger, times(1)).warn("wrongCategory event category configured in non exist" +
                " job config is not present in event_records table in openmrs database");

    }
}