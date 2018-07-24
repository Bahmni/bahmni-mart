package org.bahmni.mart.exports.writer;

import org.bahmni.mart.exports.updatestrategy.IncrementalUpdateStrategy;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Component
@Scope(value = "prototype")
public class RemovalWriter extends BaseWriter implements ItemWriter<Map<String, Object>> {

    private TableData tableData;

    @Override
    public void write(List<? extends Map<String, Object>> items) throws Exception {
        deletedVoidedRecords(items, incrementalStrategyContext.getStrategy(jobDefinition.getType()), tableData);
    }

    private void deletedVoidedRecords(List<?> items, IncrementalUpdateStrategy incrementalUpdater,
                                      TableData tableData) {
        if (isMetadataSame(incrementalUpdater, tableData.getName())) {
            deleteVoidedRecords(items, tableData, incrementalUpdater);
        }
    }

    private boolean isMetadataSame(IncrementalUpdateStrategy incrementalUpdater, String tableName) {
        return !isNull(jobDefinition) && !isNull(jobDefinition.getIncrementalUpdateConfig()) &&
                !incrementalUpdater.isMetaDataChanged(tableName, jobDefinition.getName());
    }

    private void deleteVoidedRecords(List<?> items, TableData tableData, IncrementalUpdateStrategy incrementalUpdater) {
        Set<String> voidedIds = getVoidedIds(items);
        incrementalUpdater.deleteVoidedRecords(voidedIds, tableData.getName(),
                jobDefinition.getIncrementalUpdateConfig().getUpdateOn());

    }

    private Set<String> getVoidedIds(List<?> items) {
        String updateOn = jobDefinition.getIncrementalUpdateConfig().getUpdateOn();
        return ((List<Map<String, Object>>) items).stream().map(record -> String.valueOf(record.get(updateOn)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }
}
