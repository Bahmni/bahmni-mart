package org.bahmni.mart.helper;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.exports.model.EventInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class EventManager {
    private static final String QUERY_FOR_URL_EXTRACTION = "SELECT DISTINCT object FROM event_records" +
            " WHERE id > %s AND id <= %s AND binary category = '%s'";

    private static final String UUID_REGEXP = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEXP);

    @Autowired
    @Qualifier("openmrsJdbcTemplate")
    private JdbcTemplate openmrsJdbcTemplate;

    private final HashMap<EventInfo, List<String>> alreadyEvaluatedEventMap;


    public EventManager() {
        alreadyEvaluatedEventMap = new HashMap<>();
    }

    public List<String> getEventRecordUuids(EventInfo eventInfo) {
        if (!alreadyEvaluatedEventMap.containsKey(eventInfo)) {
            alreadyEvaluatedEventMap.put(eventInfo, evaluateEventRecordUuids(eventInfo));
        }

        return alreadyEvaluatedEventMap.get(eventInfo);
    }

    private List<String> evaluateEventRecordUuids(EventInfo eventInfo) {
        String queryForEventRecordObjectUrls = String.format(QUERY_FOR_URL_EXTRACTION, eventInfo.getEventRecordId(),
                eventInfo.getMaxEventRecordId(), eventInfo.getCategory());

        return openmrsJdbcTemplate.queryForList(queryForEventRecordObjectUrls, String.class).stream()
                .map(this::getUuid).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    private String getUuid(String url) {
        Matcher matcher = UUID_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(0) : null;
    }
}
