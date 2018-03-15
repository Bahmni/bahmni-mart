package org.bahmni.mart.table;

import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class TableDataExtractorTest {

    private TableDataExtractor tableDataExtractor;

    @Before
    public void setUp() {
        tableDataExtractor = new TableDataExtractor();
    }

    @Test
    public void shouldExtractTableDataFromResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        String columnName = "column_name";
        when(resultSetMetaData.getColumnLabel(1)).thenReturn(columnName);
        String columnType = "integer";
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn(columnType);

        TableData expectedTableData = new TableData();
        TableColumn tableColumn = new TableColumn();
        tableColumn.setName(columnName);
        tableColumn.setType(columnType);
        expectedTableData.setColumns(Arrays.asList(tableColumn));

        TableData actualTableData = tableDataExtractor.extractData(resultSet);

        assertNotNull(actualTableData);
        assertEquals(1, actualTableData.getColumns().size());
        assertEquals(columnName, actualTableData.getColumns().get(0).getName());
        assertEquals(columnType, actualTableData.getColumns().get(0).getType());
    }
}