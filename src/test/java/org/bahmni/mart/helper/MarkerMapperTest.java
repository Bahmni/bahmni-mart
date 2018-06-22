package org.bahmni.mart.helper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MarkerMapperTest {

    @Mock
    private JdbcTemplate martJdbcTemplate;

    private MarkerMapper markerMapper;

    @Before
    public void setUp() throws Exception {
        markerMapper = new MarkerMapper();
        setValuesForMemberFields(markerMapper, "martJdbcTemplate", martJdbcTemplate);
    }

    @Test
    public void shouldQueryForMarkersWhenMarkerMapListIsNull() {
        String markersQuery = "SELECT * FROM markers";
        List<Map<String, Object>> markerMapList = new ArrayList<>();
        Map<String, Object> obsMarkerMap = new HashMap<>();
        obsMarkerMap.put("job_name", "Obs");
        markerMapList.add(obsMarkerMap);
        when(martJdbcTemplate.queryForList(markersQuery)).thenReturn(markerMapList);

        Optional<Map<String, Object>> actualObsMarkerMap = markerMapper.getJobMarkerMap("Obs");

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
        setValuesForMemberFields(markerMapper, "markerMapList", markerMapList);

        Optional<Map<String, Object>> actualObsMarkerMap = markerMapper.getJobMarkerMap("Obs");

        verify(martJdbcTemplate, times(0)).queryForList(markersQuery);
        assertTrue(actualObsMarkerMap.isPresent());
        assertEquals(obsMarkerMap, actualObsMarkerMap.get());
    }

    @Test
    public void shouldReturnEmptyOptionalWhenMarkerTableIsNotPresent() {
        String markersQuery = "SELECT * FROM markers";
        when(martJdbcTemplate.queryForList(markersQuery)).thenThrow(BadSqlGrammarException.class);

        Optional<Map<String, Object>> actualMarkerMap = markerMapper.getJobMarkerMap("Obs");

        verify(martJdbcTemplate).queryForList(markersQuery);
        assertEquals(Optional.empty(), actualMarkerMap);
    }
}