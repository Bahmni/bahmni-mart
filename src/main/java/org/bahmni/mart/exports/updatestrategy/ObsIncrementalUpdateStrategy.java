package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static org.bahmni.mart.table.SpecialCharacterResolver.getActualTableName;

@Component
@Qualifier("obsIncrementalStrategy")
public class ObsIncrementalUpdateStrategy extends AbstractIncrementalUpdateStrategy {

    private static final String QUERY_FOR_MAX_OBS_ID = "SELECT MAX(obs_id) FROM obs";
    private static final String QUERY_FOR_FULL_LOAD_WITH_OBS_ID_CONDITION = "%s AND obs0.obs_id <=%d";
    private static final String QUERY_FOR_INCREMENTAL_LOAD_WITH_OBS_ID_CONDITION = "%s AND obs_id <=%d";
    Integer maxObsId;

    @Autowired
    private TableMetadataGenerator tableMetadataGenerator;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Override
    public String updateReaderSql(String readerSql, String jobName, String updateOn) {
        Optional<Map<String, Object>> jobMarkerMap = getJobMarkerMap(jobName);
        return isFullLoad(jobMarkerMap) ?  getSqlForFullLoadWithMaxObsLimit(readerSql)
                : getSqlForIncrementalLoadWithMaxObsLimit(readerSql, updateOn, jobMarkerMap);

    }

    public String updateReaderSql(String sql, String jobName, String updateOn, String formName) {
        return isMetaDataChanged(formName, jobName) ? getSqlForFullLoadWithMaxObsLimit(sql) :
                updateReaderSql(sql, jobName, updateOn);
    }

    private String getSqlForIncrementalLoadWithMaxObsLimit(String readerSql, String updateOn,
                                                           Optional<Map<String, Object>> jobMarkerMap) {
        String sqlForIncrementalUpdate = getSqlForIncrementalUpdate(readerSql, updateOn, jobMarkerMap);
        return String.format(QUERY_FOR_INCREMENTAL_LOAD_WITH_OBS_ID_CONDITION, sqlForIncrementalUpdate, maxObsId);
    }

    private String getSqlForFullLoadWithMaxObsLimit(String readerSql) {
        return String.format(QUERY_FOR_FULL_LOAD_WITH_OBS_ID_CONDITION, readerSql, maxObsId);
    }


    @Override
    public boolean getMetaDataChangeStatus(String processedName, String jobName) {
        JobDefinition obsJobDefinition = jobDefinitionReader.getJobDefinitionByName(jobName);
        if (isNull(obsJobDefinition) || isNull(obsJobDefinition.getIncrementalUpdateConfig())) {
            return true;
        }
        TableData currentTableData = tableMetadataGenerator.getTableDataByName(getActualTableName(processedName));
        if(currentTableData == null) return false;
        SpecialCharacterResolver.resolveTableData(currentTableData);

        TableData existingTableData = getExistingTableData(processedName);
        return !currentTableData.equals(existingTableData);
    }

    void setTableMetadataGenerator(TableMetadataGenerator tableMetadataGenerator) {
        this.tableMetadataGenerator = tableMetadataGenerator;
    }

    @PostConstruct
    private void initializeMaxObsId() {
        this.maxObsId = openmrsJdbcTemplate.queryForObject(QUERY_FOR_MAX_OBS_ID, Integer.class);
    }
}
