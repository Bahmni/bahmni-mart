package org.bahmni.mart.table;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
public class TableRecordWriterTest {

    @Mock
    private JdbcTemplate postgresJdbcTemplate;

    @Mock
    private FreeMarkerEvaluator<TableRecordHolder> tableRecordHolderFreeMarkerEvaluator;

    TableRecordWriter tableRecordWriter;
    Map<String, Object> items;
    TableData tableData;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(BatchUtils.class);
        tableRecordWriter = new TableRecordWriter();
        items = new HashMap<String, Object>() {
            {
                put("program_id", 123);
            }
        };
        tableData = new TableData();
        tableData.setName("program");
        tableRecordWriter.setTableData(tableData);
        setValuesForMemberFields(tableRecordWriter, "postgresJdbcTemplate", postgresJdbcTemplate);
        setValuesForMemberFields(tableRecordWriter, "tableRecordHolderFreeMarkerEvaluator",
                tableRecordHolderFreeMarkerEvaluator);
    }

    @Test
    public void shouldExecuteEvaluatedInsertSql() throws Exception {
        String sql = "";
        when(tableRecordHolderFreeMarkerEvaluator.evaluate(anyString(), any(TableRecordHolder.class))).thenReturn(sql);

        tableRecordWriter.write(Arrays.asList(items));

        verify(postgresJdbcTemplate, times(1)).execute(sql);
        verify(tableRecordHolderFreeMarkerEvaluator, times(1)).evaluate(anyString(), any(TableRecordHolder.class));
    }

    private void setValuesForMemberFields(Object observationExportStep, String fieldName, Object valueForMemberField)
            throws NoSuchFieldException, IllegalAccessException {
        Field f1 = observationExportStep.getClass().getDeclaredField(fieldName);
        f1.setAccessible(true);
        f1.set(observationExportStep, valueForMemberField);
    }
}