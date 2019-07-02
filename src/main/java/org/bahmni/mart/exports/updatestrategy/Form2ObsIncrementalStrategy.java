package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.table.Form2TableMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Component
@Qualifier("form2ObsIncrementalStrategy")
public class Form2ObsIncrementalStrategy extends ObsIncrementalUpdateStrategy {

    private static final String QUERY_FOR_FULL_LOAD_WITH_OBS_ID_CONDITION = "%s WHERE o.obs_id <=%d";
    private static final String QUERY_FOR_INCREMENTAL_LOAD_WITH_OBS_ID_CONDITION = "%s AND id <=%d";

    @Autowired
    private Form2TableMetadataGenerator form2TableMetadataGenerator;

    @Override
    public String updateReaderSql(String readerSql, String jobName, String updateOn) {
        Optional<Map<String, Object>> jobMarkerMap = super.getJobMarkerMap(jobName);
        return isFullLoad(jobMarkerMap) ?  getSqlForFullLoadWithMaxObsLimit(readerSql)
                : getSqlForIncrementalLoadWithMaxObsLimit(readerSql, updateOn, jobMarkerMap);

    }

    public String updateReaderSql(String sql, String jobName, String updateOn, String formName) {
        return super.isMetaDataChanged(formName, jobName) ? getSqlForFullLoadWithMaxObsLimit(sql) :
                updateReaderSql(sql, jobName, updateOn);
    }

    private String getSqlForIncrementalLoadWithMaxObsLimit(String readerSql, String updateOn,
                                                           Optional<Map<String, Object>> jobMarkerMap) {
        String sqlForIncrementalUpdate = super.getSqlForIncrementalUpdate(readerSql, updateOn, jobMarkerMap);
        return String.format(QUERY_FOR_INCREMENTAL_LOAD_WITH_OBS_ID_CONDITION, sqlForIncrementalUpdate, maxObsId);
    }

    private String getSqlForFullLoadWithMaxObsLimit(String readerSql) {
        return String.format(QUERY_FOR_FULL_LOAD_WITH_OBS_ID_CONDITION, readerSql, maxObsId);
    }

    @PostConstruct
    public void postConstruct() {
        super.setTableMetadataGenerator(form2TableMetadataGenerator);
    }
}
