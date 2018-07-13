package org.bahmni.mart.table;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.exports.updatestrategy.ObsIncrementalUpdateStrategy;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SpecialCharacterResolver.class)
public class TableGeneratorStepTest {

    private static final String JOB_TYPE = "Obs";
    private TableGeneratorStep tableGeneratorStep;

    private static final String JOB_NAME = "Obs Data";

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private FreeMarkerEvaluator<TableData> freeMarkerEvaluatorForTables;

    @Mock
    private ObsIncrementalUpdateStrategy obsIncrementalUpdater;

    @Mock
    private IncrementalStrategyContext incrementalStrategyContext;

    @Before
    public void setUp() throws Exception {
        mockStatic(SpecialCharacterResolver.class);

        tableGeneratorStep = new TableGeneratorStep();
        setValuesForMemberFields(tableGeneratorStep, "martJdbcTemplate", martJdbcTemplate);
        setValuesForMemberFields(tableGeneratorStep, "freeMarkerEvaluatorForTables", freeMarkerEvaluatorForTables);
        setValuesForMemberFields(tableGeneratorStep, "incrementalStrategyContext", incrementalStrategyContext);
        when(incrementalStrategyContext.getStrategy(anyString())).thenReturn(obsIncrementalUpdater);
        when(jobDefinition.getType()).thenReturn(JOB_TYPE);
        when(jobDefinition.getName()).thenReturn(JOB_NAME);
    }

    @Test
    public void shouldCreateTablesWhenThereIsMetadataChange() {
        String formName = "form, name@one";
        String actualTableName = "form,_name@one";
        TableData tableData = new TableData(formName);
        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("id_formnameone", "integer", true, null));
        tableColumns.add(new TableColumn("field_1", "int", false, null));
        tableData.setColumns(tableColumns);

        when(obsIncrementalUpdater.isMetaDataChanged(actualTableName, JOB_NAME)).thenReturn(true);
        when(SpecialCharacterResolver.getActualTableName(anyString())).thenReturn(actualTableName);
        String sql = "some sql";
        when(freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableData)).thenReturn(sql);

        tableGeneratorStep.createTables(Collections.singletonList(tableData), jobDefinition);

        verifyStatic(times(1));
        SpecialCharacterResolver.resolveTableData(tableData);

        verifyStatic(times(1));
        SpecialCharacterResolver.getActualTableName(formName);

        verify(incrementalStrategyContext).getStrategy(JOB_TYPE);
        verify(obsIncrementalUpdater).isMetaDataChanged(actualTableName, JOB_NAME);
        verify(jobDefinition).getType();
        verify(freeMarkerEvaluatorForTables).evaluate("ddlForForm.ftl", tableData);
        verify(martJdbcTemplate).execute(sql);
    }

    @Test
    public void shouldNotCreateTableWhenThereIsNoMetaDataChange() {
        String formNameOne = "form, name";
        String actualTableNameOne = "form,_name";
        when(SpecialCharacterResolver.getActualTableName(formNameOne)).thenReturn(actualTableNameOne);
        TableData tableDataOne = new TableData(formNameOne);
        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("id_formnameone", "integer", true, null));
        tableColumns.add(new TableColumn("field_1", "int", false, null));
        tableDataOne.setColumns(tableColumns);

        String formNameTwo = "form @name";
        String actualTableNameTwo = "form_@name";
        when(SpecialCharacterResolver.getActualTableName(formNameTwo)).thenReturn(actualTableNameTwo);
        TableData tableDataTwo = new TableData(formNameTwo);
        when(freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableDataOne)).thenReturn("table one sql");
        when(freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableDataTwo)).thenReturn("table two sql");
        when(obsIncrementalUpdater.isMetaDataChanged(actualTableNameOne, JOB_NAME)).thenReturn(true);
        when(obsIncrementalUpdater.isMetaDataChanged(actualTableNameTwo, JOB_NAME)).thenReturn(false);

        tableGeneratorStep.createTables(Arrays.asList(tableDataOne, tableDataTwo), jobDefinition);

        verify(obsIncrementalUpdater).isMetaDataChanged(actualTableNameOne,JOB_NAME);
        verify(obsIncrementalUpdater).isMetaDataChanged(actualTableNameTwo, JOB_NAME);
        verify(jobDefinition).getType();
        verify(incrementalStrategyContext).getStrategy(JOB_TYPE);
        verify(freeMarkerEvaluatorForTables).evaluate("ddlForForm.ftl", tableDataOne);
        verify(martJdbcTemplate).execute("table one sql");
        verify(freeMarkerEvaluatorForTables, never()).evaluate("ddlForForm.ftl", tableDataTwo);
        verify(martJdbcTemplate, never()).execute("table two sql");
        
        verifyStatic(times(1));
        SpecialCharacterResolver.resolveTableData(tableDataOne);

        verifyStatic(times(1));
        SpecialCharacterResolver.getActualTableName(formNameOne);

        verifyStatic(times(1));
        SpecialCharacterResolver.resolveTableData(tableDataTwo);

        verifyStatic(times(1));
        SpecialCharacterResolver.getActualTableName(formNameTwo);
    }
}