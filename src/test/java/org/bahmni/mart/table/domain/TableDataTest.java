package org.bahmni.mart.table.domain;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TableDataTest {

    private TableData tableData;

    @Before
    public void setUp() throws Exception {
        tableData = new TableData();
    }

    @Test
    public void shouldReturnFalseWhenTableDataComparedWithNull() {
        assertFalse(tableData.equals(null));
    }

    @Test
    public void shouldReturnFalseWhenTableDataComparedWithOtherClassInstance() {
        Integer otherClassInstance = 0;

        assertFalse(tableData.equals(otherClassInstance));
    }

    @Test
    public void shouldReturnTrueWhenTableDataComparedWithItself() {
        assertTrue(tableData.equals(tableData));
    }

    @Test
    public void shouldReturnFalseWhenTableNameDoesNotMatch() {
        tableData.setName("tableName");

        TableData otherTableData = new TableData("otherTableName");

        assertFalse(tableData.equals(otherTableData));
    }

    @Test
    public void shouldReturnTrueGivenTwoTableDataWithSameContentsHavingOneColumnPerTableData() {
        tableData.setName("tableName");
        TableColumn tableColumn = new TableColumn("column", "int", true, null);
        tableData.setColumns(Collections.singletonList(tableColumn));

        TableData otherTableData = new TableData("tableName");
        TableColumn otherTableColumn = new TableColumn("column", "int", true, null);
        otherTableData.setColumns(Collections.singletonList(otherTableColumn));

        assertTrue(tableData.equals(otherTableData));
    }

    @Test
    public void shouldReturnTrueGivenTwoTableDataWithSameContentsHavingMultipleColumnsPerTableData() {
        tableData.setName("tableName");
        TableColumn tableColumnOne = new TableColumn("column1", "int", true, null);
        TableColumn tableColumnTwo = new TableColumn("column2", "int", false, null);
        tableData.setColumns(Arrays.asList(tableColumnOne, tableColumnTwo));

        TableData otherTableData = new TableData("tableName");
        TableColumn otherTableColumnOne = new TableColumn("column1", "int", true, null);
        TableColumn otherTableColumnTwo = new TableColumn("column2", "int", false, null);
        otherTableData.setColumns(Arrays.asList(otherTableColumnOne, otherTableColumnTwo));

        assertTrue(tableData.equals(otherTableData));
    }

    @Test
    public void shouldReturnTrueGivenTwoTableDataWithSameContentsHavingMultipleColumnsPerTableDataInAnyOrder() {
        tableData.setName("tableName");
        TableColumn tableColumnOne = new TableColumn("column1", "int", true, null);
        TableColumn tableColumnTwo = new TableColumn("column2", "text", false, null);
        tableData.setColumns(Arrays.asList(tableColumnOne, tableColumnTwo));

        TableData otherTableData = new TableData("tableName");
        TableColumn otherTableColumnOne = new TableColumn("column1", "int", true, null);
        TableColumn otherTableColumnTwo = new TableColumn("column2", "text", false, null);
        otherTableData.setColumns(Arrays.asList(otherTableColumnTwo, otherTableColumnOne));

        assertTrue(tableData.equals(otherTableData));
    }

    @Test
    public void shouldReturnTrueGivenTwoTableDataColumnListAsNull() {
        tableData.setName("tableName");
        tableData.setColumns(null);

        TableData otherTableData = new TableData("tableName");
        otherTableData.setColumns(null);

        assertTrue(tableData.equals(otherTableData));
    }

    @Test
    public void shouldReturnFalseGivenFirstTableDataColumnListAsNull() {
        tableData.setName("tableName");
        tableData.setColumns(null);

        TableData otherTableData = new TableData("tableName");
        TableColumn otherTableColumnOne = new TableColumn("column1", "int", true, null);
        TableColumn otherTableColumnTwo = new TableColumn("column2", "int", false, null);
        otherTableData.setColumns(Arrays.asList(otherTableColumnOne, otherTableColumnTwo));

        assertFalse(tableData.equals(otherTableData));
    }

    @Test
    public void shouldReturnFalseGivenSecondTableDataColumnListAsNull() {
        tableData.setName("tableName");
        TableColumn tableColumnOne = new TableColumn("column1", "int", true, null);
        TableColumn tableColumnTwo = new TableColumn("column2", "int", false, null);
        tableData.setColumns(Arrays.asList(tableColumnOne, tableColumnTwo));

        TableData otherTableData = new TableData("tableName");
        otherTableData.setColumns(null);

        assertFalse(tableData.equals(otherTableData));
    }

    @Test
    public void shouldReturnFalseGivenTwoTableDataColumnListsAreDifferent() {
        tableData.setName("tableName");
        TableColumn tableColumnOne = new TableColumn("column1", "int", true, null);
        TableColumn tableColumnTwo = new TableColumn("column2", "text", false, null);
        tableData.setColumns(Arrays.asList(tableColumnOne, tableColumnTwo));

        TableData otherTableData = new TableData("tableName");
        TableColumn otherTableColumnOne = new TableColumn("column", "text", false, null);
        TableColumn otherTableColumnTwo = new TableColumn("column", "int", true, null);
        otherTableData.setColumns(Arrays.asList(otherTableColumnTwo, otherTableColumnOne));

        assertFalse(tableData.equals(otherTableData));
    }
}