package org.bahmni.mart.exports.writer;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.TableRecordHolder;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class TableRecordWriterTest {

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private FreeMarkerEvaluator<TableRecordHolder> tableRecordHolderFreeMarkerEvaluator;

    @Mock
    private TableData tableData;

    private TableRecordWriter tableRecordWriter;
    private Map<String, Object> items;
    private static final String JOB_NAME = "jobName";
    private static final String TABLE_NAME = "tableName";


    @Before
    public void setUp() throws Exception {
        mockStatic(BatchUtils.class);
        tableRecordWriter = new TableRecordWriter();
        items = new HashMap<String, Object>() {
            {
                put("program_id", 123);
            }
        };
        when(tableData.getName()).thenReturn(TABLE_NAME);
        tableRecordWriter.setTableData(tableData);
        setValuesForSuperClassMemberFields(tableRecordWriter, "martJdbcTemplate", martJdbcTemplate);
        setValuesForMemberFields(tableRecordWriter, "tableRecordHolderFreeMarkerEvaluator",
                tableRecordHolderFreeMarkerEvaluator);
    }

    @Test
    public void shouldExecuteEvaluatedInsertSql() {
        String sql = "";
        when(tableRecordHolderFreeMarkerEvaluator.evaluate(anyString(), any(TableRecordHolder.class))).thenReturn(sql);

        tableRecordWriter.write(Collections.singletonList(items));

        verify(martJdbcTemplate, times(1)).execute(sql);
        verify(tableRecordHolderFreeMarkerEvaluator, times(1)).evaluate(anyString(), any(TableRecordHolder.class));
    }

}