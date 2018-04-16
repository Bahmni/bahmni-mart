package org.bahmni.mart.helper;

import org.bahmni.mart.table.TableDataExtractor;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class TableDataGenerator {

    private static final String LIMIT = " LIMIT 1";

    @Qualifier("openmrsJdbcTemplate")
    @Autowired
    private JdbcTemplate openmrsJdbcTemplate;

    public TableData getTableData(String tableName, String sql) {
        ResultSetExtractor<TableData> resultSetExtractor = new TableDataExtractor();
        TableData tableData = openmrsJdbcTemplate.query(sql + LIMIT, resultSetExtractor);
        tableData.setName(tableName);
        tableData.getColumns().forEach(tableColumn
            -> tableColumn.setType(Constants.getPostgresDataTypeFor(tableColumn.getType())));

        return tableData;
    }
}
