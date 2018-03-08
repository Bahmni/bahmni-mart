package com.bahmni.batch.bahmnianalytics.table;

import com.bahmni.batch.bahmnianalytics.table.domain.TableColumn;
import com.bahmni.batch.bahmnianalytics.table.domain.TableData;
import com.bahmni.batch.bahmnianalytics.util.BatchUtils;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TableDataProcessor implements ItemProcessor<Map<String, Object>, Map<String, Object>> {

    private TableData tableData;

    @Override
    public Map<String, Object> process(Map<String, Object> item) throws Exception {
        Map<String, Object> processedMap = item.entrySet().stream().collect(
            Collectors.toMap(p -> p.getKey(),
                p -> getProcessedValue(p.getKey(), p.getValue())));
        return processedMap;
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
        Optional<TableColumn> col = tableData.getColumns().stream().filter(column -> column.getName().equals(key)).findFirst();
        return BatchUtils.getPostgresCompatibleValue(value.toString(), col.get().getType());
    }
}
