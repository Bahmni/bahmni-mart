package org.bahmni.mart.helper;

import org.bahmni.mart.table.FormTableMetadataGenerator;
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

    @Qualifier("martJdbcTemplate")
    @Autowired
    private JdbcTemplate martJdbcTemplate;

    public TableData getTableDataFromOpenmrs(String tableName, String sql) {
        return getTableDataFrom(openmrsJdbcTemplate, tableName, sql);
    }

    public TableData getTableDataFromMart(String tableName, String sql) {
        return getTableDataFrom(martJdbcTemplate, tableName, sql);
    }

    private TableData getTableDataFrom(JdbcTemplate jdbcTemplate, String tableName, String sql) {
        ResultSetExtractor<TableData> resultSetExtractor = new TableDataExtractor();
        TableData tableData = jdbcTemplate.query(sql + LIMIT, resultSetExtractor);
        tableData.setName(FormTableMetadataGenerator.getProcessedName(tableName));
        tableData.getColumns().forEach(tableColumn
            -> tableColumn.setType(Constants.getPostgresDataTypeFor(tableColumn.getType())));

        return tableData;
    }
}
