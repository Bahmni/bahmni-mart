package org.bahmni.mart.exports.writer;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.CustomSqlIncrementalUpdateStrategy;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.TableRecordHolder;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
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
    private CustomSqlIncrementalUpdateStrategy customSqlIncrementalUpdater;

    @Mock
    private IncrementalStrategyContext incrementalStrategyContext;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private IncrementalUpdateConfig incrementalUpdateConfig;

    private TableRecordWriter tableRecordWriter;
    private Map<String, Object> items;
    private static final String JOB_NAME = "jobName";


    @Before
    public void setUp() throws Exception {
        mockStatic(BatchUtils.class);
        tableRecordWriter = new TableRecordWriter();
        items = new HashMap<String, Object>() {
            {
                put("program_id", 123);
            }
        };
        TableData tableData = new TableData();
        tableData.setName("program");
        when(jobDefinition.getName()).thenReturn(JOB_NAME);
        tableRecordWriter.setTableData(tableData);
        tableRecordWriter.setJobDefinition(jobDefinition);
        when(jobDefinition.getType()).thenReturn("customSql");

        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(null);
        setValuesForSuperClassMemberFields(tableRecordWriter, "martJdbcTemplate", martJdbcTemplate);
        setValuesForMemberFields(tableRecordWriter, "tableRecordHolderFreeMarkerEvaluator",
                tableRecordHolderFreeMarkerEvaluator);
        setValuesForMemberFields(tableRecordWriter, "incrementalStrategyContext", incrementalStrategyContext);
        when(incrementalStrategyContext.getStrategy(anyString())).thenReturn(customSqlIncrementalUpdater);

        when(customSqlIncrementalUpdater.isMetaDataChanged(anyString())).thenReturn(true);
    }

    @Test
    public void shouldExecuteEvaluatedInsertSql() {
        String sql = "";
        when(tableRecordHolderFreeMarkerEvaluator.evaluate(anyString(), any(TableRecordHolder.class))).thenReturn(sql);

        tableRecordWriter.write(Arrays.asList(items));

        verify(martJdbcTemplate, times(1)).execute(sql);
        verify(tableRecordHolderFreeMarkerEvaluator, times(1)).evaluate(anyString(), any(TableRecordHolder.class));
        verify(customSqlIncrementalUpdater, never()).isMetaDataChanged(JOB_NAME);
        verify(jobDefinition).getName();
        verify(jobDefinition).getIncrementalUpdateConfig();
        verify(jobDefinition).getType();
        verify(incrementalStrategyContext).getStrategy("customSql");
        verify(customSqlIncrementalUpdater, never()).deleteVoidedRecords(any(), any(), any());
    }

    @Test
    public void shouldNotDeleteVoidedRecordsIfJobDefinitionIsNull() {
        tableRecordWriter.setJobDefinition(null);

        String sql = "";
        when(tableRecordHolderFreeMarkerEvaluator.evaluate(anyString(), any(TableRecordHolder.class))).thenReturn(sql);

        tableRecordWriter.write(Arrays.asList(items));

        verify(martJdbcTemplate, times(1)).execute(sql);
        verify(tableRecordHolderFreeMarkerEvaluator, times(1)).evaluate(anyString(), any(TableRecordHolder.class));
        verify(customSqlIncrementalUpdater, never()).isMetaDataChanged(JOB_NAME);
        verify(jobDefinition, never()).getName();
        verify(jobDefinition, never()).getIncrementalUpdateConfig();
        verify(customSqlIncrementalUpdater, never()).deleteVoidedRecords(any(), any(), any());
        verify(jobDefinition, never()).getType();
        verify(incrementalStrategyContext, never()).getStrategy("customSql");
    }

    @Test
    public void shouldDeleteVoidedRecordsBeforeInsertNewData() {
        when(customSqlIncrementalUpdater.isMetaDataChanged(anyString())).thenReturn(false);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(incrementalUpdateConfig);
        when(incrementalUpdateConfig.getUpdateOn()).thenReturn("program_id");

        String sql = "";
        when(tableRecordHolderFreeMarkerEvaluator.evaluate(anyString(), any(TableRecordHolder.class))).thenReturn(sql);

        HashMap<String, Object> record1 = new HashMap<String, Object>() {
            {
                put("program_id", 124);
            }
        };

        HashMap<String, Object> record2 = new HashMap<String, Object>() {
            {
                put("program_id", 128);
            }
        };

        tableRecordWriter.write(Arrays.asList(items, record1, record2));

        verify(martJdbcTemplate, times(1)).execute(sql);
        verify(tableRecordHolderFreeMarkerEvaluator, times(1)).evaluate(anyString(), any(TableRecordHolder.class));
        verify(customSqlIncrementalUpdater).isMetaDataChanged(JOB_NAME);
        verify(jobDefinition).getName();
        verify(jobDefinition, atLeastOnce()).getIncrementalUpdateConfig();
        verify(incrementalUpdateConfig,atLeastOnce()).getUpdateOn();
        HashSet<String> voidedIds = new HashSet<>(Arrays.asList("123", "124", "128"));
        verify(customSqlIncrementalUpdater).deleteVoidedRecords(voidedIds, "program", "program_id");
        verify(jobDefinition).getType();
        verify(incrementalStrategyContext).getStrategy("customSql");
    }
}