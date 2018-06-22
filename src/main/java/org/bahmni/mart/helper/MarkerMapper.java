package org.bahmni.mart.helper;

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

    private static final String MARKER_QUERY = "SELECT * FROM markers";

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
}
