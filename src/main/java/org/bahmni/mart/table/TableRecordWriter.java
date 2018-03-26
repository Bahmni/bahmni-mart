package org.bahmni.mart.table;

import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private TableData tableData;

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    @Override
    public void write(List<? extends Map<String, Object>> items) throws Exception {
        List<Map<String, Object>> recordList = new ArrayList<>(items);
        TableRecordHolder tableRecordHolder = new TableRecordHolder(recordList, tableData.getName());
        String sql = tableRecordHolderFreeMarkerEvaluator.evaluate("insertObs.ftl", tableRecordHolder);
        martJdbcTemplate.execute(sql);
    }
}
