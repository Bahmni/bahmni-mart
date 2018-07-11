package org.bahmni.mart.exports.writer;

import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.bahmni.mart.table.TableRecordHolder;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Component
@Scope(value = "prototype")
public class TableRecordWriter extends BaseWriter implements ItemWriter<Map<String, Object>> {

    @Autowired
    private FreeMarkerEvaluator<TableRecordHolder> tableRecordHolderFreeMarkerEvaluator;

    @Autowired
    public TableMetadataGenerator tableMetadataGenerator;

    @Autowired
    private IncrementalStrategyContext incrementalStrategyContext;

    private TableData tableData;

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    @Override
    public void write(List<? extends Map<String, Object>> items) {
        List<Map<String, Object>> records = new ArrayList<>(items);
        if (!isNull(jobDefinition))
            deletedVoidedRecords(records, incrementalStrategyContext.getStrategy(jobDefinition.getType()),
                    jobDefinition.getName(), tableData);
        insertRecords(records);
    }

    private void insertRecords(List<Map<String, Object>> recordList) {
        TableRecordHolder tableRecordHolder = new TableRecordHolder(recordList, tableData.getName());
        String sql = tableRecordHolderFreeMarkerEvaluator.evaluate("insertObs.ftl", tableRecordHolder);
        martJdbcTemplate.execute(sql);
    }

    @Override
    protected Set<String> getVoidedIds(List<?> items) {
        String updateOn = jobDefinition.getIncrementalUpdateConfig().getUpdateOn();
        return ((List<Map<String, Object>>) items).stream().map(record -> String.valueOf(record.get(updateOn)))
                .collect(Collectors.toCollection(HashSet::new));
    }
}
