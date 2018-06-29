package org.bahmni.mart.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class MarkerMapper {

    private static final Logger logger = LoggerFactory.getLogger(MarkerMapper.class);
    private static final String MARKER_QUERY = "SELECT * FROM markers";
    private static final String UPDATE_QUERY = "UPDATE markers SET event_record_id = %s WHERE job_name = '%s'";
    public static final String ERROR_INFO = "Failed to update event_record_id for %s, markers table is not present";

    @Autowired
    @Qualifier("martJdbcTemplate")
    private JdbcTemplate martJdbcTemplate;

    private List<Map<String, Object>> markerMapList;

    public Optional<Map<String, Object>> getJobMarkerMap(String jobName) {
        if (Objects.isNull(markerMapList)) {
            try {
                markerMapList = martJdbcTemplate.queryForList(MARKER_QUERY);
            } catch (BadSqlGrammarException e) {
                return Optional.empty();
            }
        }
        return markerMapList.stream()
                .filter(markerMap -> jobName.equalsIgnoreCase(String.valueOf(markerMap.get("job_name"))))
                .findFirst();
    }

    public void updateMarker(String jobName, String eventRecordId) {
        try {
            martJdbcTemplate.execute(String.format(UPDATE_QUERY, eventRecordId, jobName));
        } catch (BadSqlGrammarException e) {
            logger.error(String.format(ERROR_INFO, jobName));
        }
    }
}
