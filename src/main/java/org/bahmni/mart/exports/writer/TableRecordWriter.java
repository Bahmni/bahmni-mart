package org.bahmni.mart.exports.writer;

import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.bahmni.mart.table.TableRecordHolder;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope(value = "prototype")
public class TableRecordWriter extends BaseWriter implements ItemWriter<Map<String, Object>> {

    @Autowired
    private FreeMarkerEvaluator<TableRecordHolder> tableRecordHolderFreeMarkerEvaluator;

    @Autowired
    public TableMetadataGenerator tableMetadataGenerator;

    private TableData tableData;

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    @Override
    public void write(List<? extends Map<String, Object>> items) {
        List<Map<String, Object>> records = new ArrayList<>(items);
        insertRecords(records);
    }

    private void insertRecords(List<Map<String, Object>> recordList) {
        TableRecordHolder tableRecordHolder = new TableRecordHolder(recordList, tableData.getName());
        String sql = tableRecordHolderFreeMarkerEvaluator.evaluate("insertObs.ftl", tableRecordHolder);
        martJdbcTemplate.execute(sql);
    }
}
