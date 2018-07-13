package org.bahmni.mart.table;

import org.bahmni.mart.table.domain.ForeignKey;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.table.SpecialCharacterResolver.getActualColumnName;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public class SpecialCharacterResolverTest {

    @Test
    public void shouldRemoveCommasInTableDataAndShouldBeAbleToGetActualColumns() {
        TableData tableData = new TableData("skin,_@form_template");
        TableColumn tableColumn = new TableColumn("do,_you_@have_any_skin%_problems?", "text", false, null);
        TableColumn tableColumn1 = new TableColumn("skin,have_you,had_a_skin_rash_recently?", "text", false, null);
        TableColumn tableColumn2 = new TableColumn("skin@have_you,had_a_skin_rash_recently???", "text", false, null);
        tableData.setColumns(Arrays.asList(tableColumn, tableColumn1, tableColumn2));

        List<String> expectedColumnNames = Arrays.asList("do_you_have_any_skin_problems",
                "skin_have_you_had_a_skin_rash_recently", "skin_1have_you_1had_a_skin_rash_recently_1");

        SpecialCharacterResolver.resolveTableData(tableData);

        List<String> actualColumnNames = tableData.getColumns().stream().map(column -> column.getName())
                .collect(Collectors.toList());

        assertEquals("skin_form_template", tableData.getName());
        assertEquals(3, tableData.getColumns().size());
        assertThat(expectedColumnNames, containsInAnyOrder(actualColumnNames.toArray()));
        assertEquals("do,_you_@have_any_skin%_problems?", getActualColumnName(tableData, tableColumn));
        assertEquals("skin,have_you,had_a_skin_rash_recently?", getActualColumnName(tableData, tableColumn1));
        assertEquals("skin@have_you,had_a_skin_rash_recently???", getActualColumnName(tableData, tableColumn2));

    }

    @Test
    public void shouldRemoveCommasInTableDataWithForeignKeyReferences() {

        TableData tableData = new TableData("skin1,_@form_template");
        TableColumn tableColumn = new TableColumn("do,_you_@have_any_skin%_problems?", "text", false, null);
        tableData.setColumns(Arrays.asList(tableColumn));

        TableData tableData1 = new TableData("test-table");
        TableColumn tableColumn1 = new TableColumn("test@id", "text", false, null);
        ForeignKey foreignKey = new ForeignKey("do,_you_@have_any_skin%_problems?", "skin1,_@form_template");
        tableColumn1.setReference(foreignKey);
        tableData1.setColumns(Arrays.asList(tableColumn1));

        SpecialCharacterResolver.resolveTableData(tableData);
        SpecialCharacterResolver.resolveTableData(tableData1);

        List<String> actualColumnNames = tableData1.getColumns().stream().map(column -> column.getName())
                .collect(Collectors.toList());

        assertEquals("test_table", tableData1.getName());
        assertEquals(1, tableData1.getColumns().size());
        assertThat(Arrays.asList("test_id"), containsInAnyOrder(actualColumnNames.toArray()));
        ForeignKey actualReference = tableData1.getColumns().get(0).getReference();
        assertEquals("skin1_form_template", actualReference.getReferenceTable());
        assertEquals("do_you_have_any_skin_problems", actualReference.getReferenceColumn());
    }

    @Test
    public void shouldNotRemoveSpecialCharactersInForeignKeyIfParentTableSpecialCharactersAreNotRemoved() {

        TableData tableData = new TableData("skin2,_@form_template");
        TableColumn tableColumn = new TableColumn("do,_you_@have_any_skin%_problems?", "text", false, null);
        tableData.setColumns(Arrays.asList(tableColumn));

        TableData tableData1 = new TableData("test-table1");
        TableColumn tableColumn1 = new TableColumn("test@id1", "text", false, null);
        ForeignKey foreignKey = new ForeignKey("do,_you_@have_any_skin%_problems?", "skin2,_@form_template");
        tableColumn1.setReference(foreignKey);
        tableData1.setColumns(Arrays.asList(tableColumn1));

        SpecialCharacterResolver.resolveTableData(tableData1);

        List<String> actualColumnNames = tableData1.getColumns().stream().map(column -> column.getName())
                .collect(Collectors.toList());

        assertEquals("test_table1", tableData1.getName());
        assertEquals(1, tableData1.getColumns().size());
        assertThat(Arrays.asList("test_id1"), containsInAnyOrder(actualColumnNames.toArray()));
        ForeignKey actualReference = tableData1.getColumns().get(0).getReference();
        assertNotEquals("skin2_form_template", actualReference.getReferenceTable());
        assertNotEquals("do_you_have_any_skin_problems_", actualReference.getReferenceColumn());
    }

    @Test
    public void shouldReturnActualTableNameIfSpecialCharacterNotRemovedYet() {

        String updatedTableName = SpecialCharacterResolver.getUpdatedTableNameIfExist("some@table");

        assertEquals("some@table", updatedTableName);
    }

    @Test
    public void shouldReturnActualColumnNameIfSpecialCharacterNotRemovedYet() {

        TableData tableData = new TableData("test-table2");
        TableColumn tableColumn = new TableColumn("test@id2", "text", false, null);

        String updatedTableName = SpecialCharacterResolver.getActualColumnName(tableData, tableColumn);

        assertEquals("test@id2", updatedTableName);
    }

    @Test
    public void shouldReturnActualTableForGivenUpdatedTableName() {

        String expectedTableName = "form,name@one";
        TableData tableData = new TableData(expectedTableName);
        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("id_formnameone","integer",true,null));
        tableColumns.add(new TableColumn("field","int",false,null));
        tableData.setColumns(tableColumns);

        SpecialCharacterResolver.resolveTableData(tableData);

        String updatedTableName = tableData.getName();
        assertEquals("form_name_one", updatedTableName);

        String actualTableName = SpecialCharacterResolver.getActualTableName(updatedTableName);
        assertEquals(expectedTableName, actualTableName);
    }

    @Test
    public void shouldReturnSameNameIfActualTableForGivenUpdatedTableNameIsNotPresent() {
        String tableName = "test table";

        String actualTableName = SpecialCharacterResolver.getActualTableName(tableName);
        assertEquals(tableName, actualTableName);
    }
}

