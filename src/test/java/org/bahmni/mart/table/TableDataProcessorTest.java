package org.bahmni.mart.table;

import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TableDataProcessorTest {

    private TableDataProcessor tableDataProcessor;
    private Map<String, Object> items;
    private TableData tableData;

    @Before
    public void setUp() throws Exception {
        tableDataProcessor = new TableDataProcessor();
        items = new HashMap<String, Object>() {
            {
                put("program_id", 123);
                put("name", "HIV");
                put("date", "12/12/2017");
            }
        };
        tableData = new TableData();
        TableColumn tableColumn1 = new TableColumn("program_id", "integer", true, null);
        TableColumn tableColumn2 = new TableColumn("name", "text", false, null);
        TableColumn tableColumn3 = new TableColumn("date", "date", false, null);
        tableData.addAllColumns(Arrays.asList(tableColumn1, tableColumn2, tableColumn3));
        tableDataProcessor.setTableData(tableData);
    }

    @Test
    public void shouldReturnProcessedMapGivenItemsMap() throws Exception {

        Map<String, Object> processedMap = tableDataProcessor.process(items);

        assertNotNull(processedMap);
        assertTrue(processedMap.keySet().contains("program_id"));
        assertTrue(processedMap.keySet().contains("name"));
        assertTrue(processedMap.keySet().contains("date"));
        assertThat(processedMap.get("program_id"), is("123"));
        assertThat(processedMap.get("name"), is("\'HIV\'"));
        assertThat(processedMap.get("date"), is("\'12/12/2017\'"));
    }

    @Test
    public void shouldReturnEmptyStringGivenItemValueIsNull() throws Exception {
        items.put("outcome_id", null);
        TableColumn tableColumn4 = new TableColumn("outcome_id", "integer", false, null);
        tableData.addColumn(tableColumn4);

        Map<String, Object> processedMap = tableDataProcessor.process(items);

        assertNotNull(processedMap);
        assertTrue(processedMap.keySet().contains("outcome_id"));
        assertThat(processedMap.get("outcome_id"), is(""));
    }
}