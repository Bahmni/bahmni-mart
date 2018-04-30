package org.bahmni.mart.table;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class TableDataProcessor implements ItemProcessor<Map<String, Object>, Map<String, Object>> {

    private TableData tableData;

    private PreProcessor preProcessor;

    @Override
    public Map<String, Object> process(Map<String, Object> item) throws Exception {
        return mapProcessedValue(preProcessor != null ? preProcessor.process(item) : item);
    }

    private Map<String, Object> mapProcessedValue(Map<String, Object> item) {
        return item.entrySet().stream().collect(Collectors.toMap(
            currentColumn -> getUpdatedColumnName(currentColumn.getKey()),
            currentColumn -> getProcessedValue(currentColumn.getKey(), currentColumn.getValue())));
    }

    private String getUpdatedColumnName(String actualColumnName) {
        return SpecialCharacterResolver.getUpdatedColumnName(tableData, actualColumnName);
    }

    public TableData getTableData() {
        return tableData;
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    private String getProcessedValue(String key, Object value) {
        if (value == null || isEmpty(value.toString())) {
            return "";
        }
        Optional<TableColumn> tableColumn = tableData.getColumns().stream()
                .filter(column -> SpecialCharacterResolver
                        .getActualColumnName(tableData, column).equals(key)).findFirst();
        return BatchUtils.getPostgresCompatibleValue(value.toString(), tableColumn.get().getType());
    }

    public void setPreProcessor(PreProcessor preProcessor) {
        this.preProcessor = preProcessor;
    }
}
