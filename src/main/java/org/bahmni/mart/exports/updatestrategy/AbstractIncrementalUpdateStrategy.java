package org.bahmni.mart.exports.updatestrategy;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.mart.helper.MarkerManager;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.bahmni.mart.table.FormTableMetadataGenerator.getProcessedName;

public abstract class AbstractIncrementalUpdateStrategy implements IncrementalUpdateStrategy {

    private static final String EVENT_RECORD_ID = "event_record_id";
    private static final String CATEGORY = "category";
    private static final String TABLE_NAME = "table_name";
    private static final String QUERY_FOR_UUID_EXTRACTION = "SELECT DISTINCT substring_index(substring_index(object, " +
            "'/', -1), '?', 1) as uuid FROM event_records WHERE id > %s AND id <= %s AND category = '%s'";
    private static final String QUERY_FOR_ID_EXTRACTION = "SELECT %s_id FROM %s WHERE uuid in (%s)";
    private static final String UPDATED_READER_SQL = "SELECT * FROM ( %s ) result WHERE %s IN (%s)";
    private static final String NON_EXISTED_ID = "-1";
    private static final String QUERY_FOR_MAX_EVENT_RECORD_ID = "SELECT MAX(id) FROM event_records";
    private static final TableData NON_EXISTENT_TABLE_DATA = new TableData();
    private static final Integer ZERO = 0;

    @Autowired
    @Qualifier("openmrsJdbcTemplate")
    private JdbcTemplate openmrsJdbcTemplate;

    @Autowired
    @Qualifier("martJdbcTemplate")
    private JdbcTemplate martJdbcTemplate;

    @Autowired
    private MarkerManager markerManager;

    @Autowired
    protected TableDataGenerator tableDataGenerator;

    private Map<String, Boolean> metaDataChangeMap = new HashMap<>();

    private Integer maxEventRecordId;

    @Override
    public String updateReaderSql(String readerSql, String jobName, String updateOn) {
        Optional<Map<String, Object>> optionalMarkerMap = markerManager.getJobMarkerMap(jobName);
        if (!optionalMarkerMap.isPresent() || ZERO.equals(optionalMarkerMap.get().get(EVENT_RECORD_ID))) {
            return readerSql;
        }
        String joinedIds = getJoinedIds(optionalMarkerMap.get());
        return String.format(UPDATED_READER_SQL, readerSql, updateOn, joinedIds);
    }

    @Override
    public void deleteVoidedRecords(Set<String> ids, String table, String column) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        String deleteSql = String.format("DELETE FROM %s WHERE %s IN (%s)", table, column, String.join(",", ids));
        martJdbcTemplate.execute(deleteSql);
    }

    public void updateMarker(String jobName) {
        if (isNull(maxEventRecordId)) {
            maxEventRecordId = ZERO;
        }
        markerManager.updateMarker(jobName, maxEventRecordId);
    }

    @Override
    public boolean isMetaDataChanged(String tableName, String jobName) {
        String actualTableName = getProcessedName(tableName);
        if (metaDataChangeMap.containsKey(actualTableName)) {
            return metaDataChangeMap.get(actualTableName);
        }
        boolean metaDataChanged = getMetaDataChangeStatus(actualTableName, jobName);
        metaDataChangeMap.put(actualTableName, metaDataChanged);
        return metaDataChanged;
    }

    private String getJoinedIds(Map<String, Object> markerMap) {
        String eventRecordId = String.valueOf(markerMap.get(EVENT_RECORD_ID));
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

    private List<String> getEventRecordUuids(String eventRecordId, String category) {
        String queryForEventRecordObjects = String.format(QUERY_FOR_UUID_EXTRACTION, eventRecordId,
                maxEventRecordId, category);
        return openmrsJdbcTemplate.queryForList(queryForEventRecordObjects, String.class);
    }

    private List<String> getIdListFor(String tableName, List<String> uuids) {
        String joinedUuids = uuids.stream().map(uuid -> String.format("'%s'", uuid)).collect(Collectors.joining(","));
        return openmrsJdbcTemplate.queryForList(String.format(QUERY_FOR_ID_EXTRACTION, tableName, tableName,
                joinedUuids), String.class);
    }

    @PostConstruct
    private void initializeMaxEventRecordId() {
        maxEventRecordId = openmrsJdbcTemplate.queryForObject(QUERY_FOR_MAX_EVENT_RECORD_ID, Integer.class);
    }

    protected abstract boolean getMetaDataChangeStatus(String actualTableName, String jobName);

    @Override
    public TableData getExistingTableData(String actualTableName) {
        String updatedTableName = SpecialCharacterResolver.getUpdatedTableNameIfExist(actualTableName);
        String sql = String.format("SELECT * FROM %s", updatedTableName);
        try {
            return tableDataGenerator.getTableDataFromMart(updatedTableName, sql);
        } catch (BadSqlGrammarException exception) {
            return NON_EXISTENT_TABLE_DATA;
        }
    }
}
