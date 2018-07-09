package org.bahmni.mart.helper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MarkerManagerTest {

    @Mock
    private JdbcTemplate martJdbcTemplate;

    private MarkerManager markerManager;

    @Mock
    private Logger logger;

    @Before
    public void setUp() throws Exception {
        markerManager = new MarkerManager();
        setValuesForMemberFields(markerManager, "martJdbcTemplate", martJdbcTemplate);
        setValueForFinalStaticField(MarkerManager.class, "logger", logger);
    }

    @Test
    public void shouldQueryForMarkersWhenMarkerMapListIsNull() {
        String markersQuery = "SELECT * FROM markers";
        List<Map<String, Object>> markerMapList = new ArrayList<>();
        Map<String, Object> obsMarkerMap = new HashMap<>();
        obsMarkerMap.put("job_name", "Obs");
        markerMapList.add(obsMarkerMap);
        when(martJdbcTemplate.queryForList(markersQuery)).thenReturn(markerMapList);

        Optional<Map<String, Object>> actualObsMarkerMap = markerManager.getJobMarkerMap("Obs");

        verify(martJdbcTemplate).queryForList(markersQuery);
        assertTrue(actualObsMarkerMap.isPresent());
        assertEquals(obsMarkerMap, actualObsMarkerMap.get());
    }

    @Test
    public void shouldNotQueryForMarkersWhenMarkerMapListIsNotNull() throws Exception {
        String markersQuery = "SELECT * FROM markers";
        List<Map<String, Object>> markerMapList = new ArrayList<>();
        Map<String, Object> obsMarkerMap = new HashMap<>();
        obsMarkerMap.put("job_name", "Obs");
        markerMapList.add(obsMarkerMap);
        setValuesForMemberFields(markerManager, "markerMapList", markerMapList);

        Optional<Map<String, Object>> actualObsMarkerMap = markerManager.getJobMarkerMap("Obs");

        verify(martJdbcTemplate, times(0)).queryForList(markersQuery);
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
}