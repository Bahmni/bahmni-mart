package org.bahmni.mart.table;

import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.table.SpecialCharacterResolver.getActualColumnName;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SpecialCharacterResolverTest {

    private TableData tableData;
    private TableColumn tableColumn;
    private TableColumn tableColumn1;
    private TableColumn tableColumn2;


    @Before
    public void setUp() {
        tableData = new TableData("skin,_@form_template");
        tableColumn = new TableColumn("do,_you_@have_any_skin%_problems?", "text", false, null);
        tableColumn1 = new TableColumn("skin,have_you,had_a_skin_rash_recently?", "text", false, null);
        tableColumn2 = new TableColumn("skin@have_you,had_a_skin_rash_recently???", "text", false, null);
        tableData.setColumns(Arrays.asList(tableColumn, tableColumn1, tableColumn2));
    }

    @Test
    public void shouldRemoveCommasInTableDataAndShouldBeAbleToGetActualColumns() {

        List<String> expectedColumnNames = Arrays.asList("do_you_have_any_skin_problems_",
                "skin_have_you_had_a_skin_rash_recently_", "skin_1have_you_1had_a_skin_rash_recently_1");

        SpecialCharacterResolver.resolveTableData(tableData);

        List<String> actualColumnNames = tableData.getColumns().stream().map(tableColumn -> tableColumn.getName())
                .collect(Collectors.toList());

        assertEquals("skin_form_template", tableData.getName());
        assertThat(expectedColumnNames, containsInAnyOrder(actualColumnNames.toArray()));
        assertEquals("do,_you_@have_any_skin%_problems?", getActualColumnName(tableData, tableColumn));
        assertEquals("skin,have_you,had_a_skin_rash_recently?", getActualColumnName(tableData, tableColumn1));
        assertEquals("skin@have_you,had_a_skin_rash_recently???", getActualColumnName(tableData, tableColumn2));

    }

}

