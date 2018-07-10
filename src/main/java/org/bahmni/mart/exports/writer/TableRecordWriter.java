package org.bahmni.mart.exports.writer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.helper.incrementalupdate.CustomSqlIncrementalUpdater;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.bahmni.mart.table.TableRecordHolder;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Component
@Scope(value = "prototype")
public class TableRecordWriter implements ItemWriter<Map<String, Object>> {

    @Qualifier("martJdbcTemplate")
    @Autowired
    private JdbcTemplate martJdbcTemplate;

    @Autowired
    private FreeMarkerEvaluator<TableRecordHolder> tableRecordHolderFreeMarkerEvaluator;

    @Autowired
    public TableMetadataGenerator tableMetadataGenerator;

    @Autowired
    private CustomSqlIncrementalUpdater customSqlIncrementalUpdater;

    private JobDefinition jobDefinition;

    private TableData tableData;

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }

    @Override
    public void write(List<? extends Map<String, Object>> items) throws Exception {
        List<Map<String, Object>> records = new ArrayList<>(items);

        if (!(isNull(jobDefinition) || customSqlIncrementalUpdater.isMetaDataChanged(jobDefinition.getName())))
            deleteVoidedRecords(records);

        insertRecords(records);
    }

    private void deleteVoidedRecords(List<Map<String, Object>> records) {
        String updateOn = jobDefinition.getIncrementalUpdateConfig().getUpdateOn();
        HashSet<String> voidedIds = records.stream().map(record -> String.valueOf(record.get(updateOn)))
                .collect(Collectors.toCollection(HashSet::new));
        customSqlIncrementalUpdater.deleteVoidedRecords(voidedIds, tableData.getName(), updateOn);
    }

    private void insertRecords(List<Map<String, Object>> recordList) {
        TableRecordHolder tableRecordHolder = new TableRecordHolder(recordList, tableData.getName());
        String sql = tableRecordHolderFreeMarkerEvaluator.evaluate("insertObs.ftl", tableRecordHolder);
        martJdbcTemplate.execute(sql);
    }
}
