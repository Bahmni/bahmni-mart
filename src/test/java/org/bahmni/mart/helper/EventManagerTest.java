package org.bahmni.mart.helper;

import org.bahmni.mart.exports.model.EventInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.String.format;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class EventManagerTest {

    @Mock
    private JdbcTemplate openmrsJdbcTemplate;

    @Mock
    private EventInfo eventInfo;

    private static final Integer MAX_EVENT_RECORD_ID = 20;
    private static final String CATEGORY = "CategoryName";
    private static final String EVENT_RECORDED_FOR_JOB = "10";

    private static final String QUERY_FOR_EVENT_URLS = "SELECT DISTINCT object " +
            "FROM event_records WHERE id > %s AND id <= %s AND binary category = '%s'";

    private EventManager eventManager;


    @Before
    public void setUp() throws Exception {
        eventManager = new EventManager();
        setValuesForMemberFields(eventManager, "openmrsJdbcTemplate", openmrsJdbcTemplate);

        List<String> eventRecordObjects = Arrays.asList(
                "/openmrs/ws/rest/v1/appointment?uuid=99b885e9-c49c-4b5a-aa4c-e8a8fb93484d",
                "/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/55527d10-5789-46e1-af16-59082938f11c?includeAll=true");

        when(eventInfo.getCategory()).thenReturn(CATEGORY);
        when(eventInfo.getEventRecordId()).thenReturn(EVENT_RECORDED_FOR_JOB);
        when(eventInfo.getMaxEventRecordId()).thenReturn(MAX_EVENT_RECORD_ID);

        when(openmrsJdbcTemplate.queryForList(
                format(QUERY_FOR_EVENT_URLS, EVENT_RECORDED_FOR_JOB, MAX_EVENT_RECORD_ID, CATEGORY), String.class))
                .thenReturn(eventRecordObjects);
    }

    @Test
    public void shouldGiveNewlyGeneratedEventRecordUuidsForGivenEventInfo() {
        List<String> eventRecordUuids = eventManager.getEventRecordUuids(eventInfo);
        List<String> expectedUuids = Arrays.asList("99b885e9-c49c-4b5a-aa4c-e8a8fb93484d",
                "55527d10-5789-46e1-af16-59082938f11c");
        assertEquals(expectedUuids, eventRecordUuids);

        verify(openmrsJdbcTemplate).queryForList(format(QUERY_FOR_EVENT_URLS, EVENT_RECORDED_FOR_JOB,
                MAX_EVENT_RECORD_ID, CATEGORY), String.class);
        verify(eventInfo).getCategory();
        verify(eventInfo).getEventRecordId();
        verify(eventInfo).getMaxEventRecordId();
    }

    @Test
    public void shouldNotCallMultipleDBCallIfItsAlreadyEvaluatedUuidsForGivenCategory() throws Exception {
        List<String> expectedUuids = Arrays.asList("99b885e9-c49c-4b5a-aa4c-e8a8fb93484d",
                "55527d10-5789-46e1-af16-59082938f11c");

        HashMap<EventInfo, List<String>> alreadyEvaluatedEventMap = new HashMap<>();
        alreadyEvaluatedEventMap.put(eventInfo, expectedUuids);

        setValuesForMemberFields(eventManager, "alreadyEvaluatedEventMap", alreadyEvaluatedEventMap);

        List<String> eventRecordUuids = eventManager.getEventRecordUuids(eventInfo);
        assertEquals(expectedUuids, eventRecordUuids);
        verify(openmrsJdbcTemplate, never()).queryForList(anyString());
    }
}