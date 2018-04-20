package org.bahmni.mart.table;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodesExtractor implements ResultSetExtractor<List<Map<String, String>>> {

    @Override
    public List<Map<String, String>> extractData(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        return getData(resultSet, getColumns(resultSetMetaData));
    }

    private List<String> getColumns(ResultSetMetaData resultSetMetaData) throws SQLException {
        int columnCount = resultSetMetaData.getColumnCount();
        List<String> columns = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            columns.add(resultSetMetaData.getColumnName(i));
        }
        return columns;
    }

    private List<Map<String, String>> getData(ResultSet resultSet, List<String> columns) throws SQLException {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        while (resultSet.next()) {
            Map<String, String> row = new HashMap<>(columns.size());
            for (String column : columns) {
                row.put(column, resultSet.getString(column));
            }
            data.add(row);
        }
        return data;
    }
}
