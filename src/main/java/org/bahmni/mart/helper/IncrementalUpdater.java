package org.bahmni.mart.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class IncrementalUpdater {

    private static final String EVENT_RECORD_ID = "event_record_id";
    private static final String CATEGORY = "category";
    private static final String TABLE_NAME = "table_name";
    private static final String QUERY_FOR_UUID_EXTRACTION = "SELECT DISTINCT substring_index(substring_index(object, " +
            "'/', -1), '?', 1) as uuid FROM event_records WHERE id > %d AND category = '%s'";
    private static final String QUERY_FOR_ID_EXTRACTION = "SELECT %s_id FROM %s WHERE uuid in (%s)";
    private static final String UPDATED_READER_SQL = "SELECT * FROM ( %s ) result WHERE %s IN (%s)";
    private static final String NON_EXISTED_ID = "-1";

    @Autowired
    @Qualifier("openmrsJdbcTemplate")
    private JdbcTemplate openmrsJdbcTemplate;

    @Autowired
    private MarkerMapper markerMapper;

    public String updateReaderSql(String readerSql, String jobName, String updateOn) {
        Optional<Map<String, Object>> optionalMarkerMap = markerMapper.getJobMarkerMap(jobName);
        if (!optionalMarkerMap.isPresent() || optionalMarkerMap.get().get(EVENT_RECORD_ID).equals(0)) {
            return readerSql;
        }
        String joinedIds = getJoinedIds(optionalMarkerMap.get());
        return String.format(UPDATED_READER_SQL, readerSql, updateOn, joinedIds);
    }

    private String getJoinedIds(Map<String, Object> markerMap) {
        Integer eventRecordId = (Integer) markerMap.get(EVENT_RECORD_ID);
        String category = (String) markerMap.get(CATEGORY);
        String tableName = (String) markerMap.get(TABLE_NAME);
        List<String> uuids = getEventRecordUuids(eventRecordId, category);
        if (uuids.isEmpty()) {
            return NON_EXISTED_ID;
        }

        return getIdListFor(tableName, uuids).stream()
                                            .map(String::valueOf)
                                            .collect(Collectors.joining(","));
    }

    private List<String> getEventRecordUuids(Integer eventRecordId, String category) {
        String queryForEventRecordObjects = String.format(QUERY_FOR_UUID_EXTRACTION, eventRecordId, category);
        return openmrsJdbcTemplate.queryForList(queryForEventRecordObjects, String.class);
    }

    private List<Long> getIdListFor(String tableName, List<String> uuids) {
        String joinedUuids = uuids.stream().map(uuid -> String.format("'%s'", uuid)).collect(Collectors.joining(","));
        return openmrsJdbcTemplate.queryForList(String.format(QUERY_FOR_ID_EXTRACTION, tableName, tableName,
                joinedUuids), Long.class);
    }
}
