package org.bahmni.mart.table;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TableDataProcessor implements ItemProcessor<Map<String, Object>, Map<String, Object>> {

    private TableData tableData;

    @Override
    public Map<String, Object> process(Map<String, Object> item) throws Exception {
        return item.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, p -> getProcessedValue(p.getKey(), p.getValue())));
    }

    public TableData getTableData() {
        return tableData;
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    private String getProcessedValue(String key, Object value) {
        if (value == null) {
            return "";
        }
        Optional<TableColumn> tableColumn = tableData.getColumns().stream()
                .filter(column -> column.getName().equals(key)).findFirst();
        return BatchUtils.getPostgresCompatibleValue(value.toString(), tableColumn.get().getType());
    }
}
