package org.bahmni.mart.table;

import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.bahmni.mart.helper.Constants.getPostgresDataTypeFor;

public class TableDataExtractor implements ResultSetExtractor<TableData> {

    @Override
    public TableData extractData(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        TableData currentTable = new TableData();

        for (int index = 1; index <= resultSetMetaData.getColumnCount(); index++) {
            String columnName = resultSetMetaData.getColumnLabel(index);
            String columnType = getPostgresDataTypeFor(resultSetMetaData.getColumnTypeName(index));
            currentTable.addColumn(new TableColumn(columnName, columnType, false, null));
        }
        return currentTable;
    }
}
