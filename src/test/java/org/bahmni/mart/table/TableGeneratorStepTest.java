package org.bahmni.mart.table;

import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.exports.updatestrategy.ObsIncrementalUpdateStrategy;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.table.FormTableMetadataGenerator.getProcessedName;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class TableGeneratorStepTest {

    private TableGeneratorStep tableGeneratorStep;

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private FreeMarkerEvaluator<TableData> freeMarkerEvaluatorForTables;

    @Mock
    private ObsIncrementalUpdateStrategy obsIncrementalUpdater;

    @Before
    public void setUp() throws Exception {
        tableGeneratorStep = new TableGeneratorStep();
        setValuesForMemberFields(tableGeneratorStep, "martJdbcTemplate", martJdbcTemplate);
        setValuesForMemberFields(tableGeneratorStep, "freeMarkerEvaluatorForTables", freeMarkerEvaluatorForTables);
        setValuesForMemberFields(tableGeneratorStep, "obsIncrementalUpdater", obsIncrementalUpdater);
    }

    @Test
    public void shouldCreateTablesWhenThereIsMetadataChange() {
        String formName = "form, name@one";
        String actualTableName = getProcessedName(formName);
        TableData tableData = new TableData(actualTableName);
        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("id_formnameone", "integer", true, null));
        tableColumns.add(new TableColumn("field_1", "int", false, null));
        tableData.setColumns(tableColumns);

        when(obsIncrementalUpdater.isMetaDataChanged(actualTableName)).thenReturn(true);
        String sql = "some sql";
        when(freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableData)).thenReturn(sql);

        tableGeneratorStep.createTablesForObs(Collections.singletonList(tableData));

        verify(obsIncrementalUpdater).isMetaDataChanged(actualTableName);
        verify(freeMarkerEvaluatorForTables).evaluate("ddlForForm.ftl", tableData);
        verify(martJdbcTemplate).execute(sql);
    }

    @Test
    public void shouldNotCreateTableWhenThereIsNoMetaDataChange() {
        String formNameOne = "form, name";
        String actualTableNameOne = getProcessedName(formNameOne);
        TableData tableDataOne = new TableData(actualTableNameOne);
        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("id_formnameone", "integer", true, null));
        tableColumns.add(new TableColumn("field_1", "int", false, null));
        tableDataOne.setColumns(tableColumns);

        String formNameTwo = "form @name";
        String actualTableNameTwo = getProcessedName(formNameTwo);
        TableData tableDataTwo = new TableData(actualTableNameTwo);
        when(freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableDataOne)).thenReturn("table one sql");
        when(freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableDataTwo)).thenReturn("table two sql");
        when(obsIncrementalUpdater.isMetaDataChanged(actualTableNameOne)).thenReturn(true);
        when(obsIncrementalUpdater.isMetaDataChanged(actualTableNameTwo)).thenReturn(false);

        tableGeneratorStep.createTablesForObs(Arrays.asList(tableDataOne, tableDataTwo));

        verify(obsIncrementalUpdater).isMetaDataChanged(actualTableNameOne);
        verify(obsIncrementalUpdater).isMetaDataChanged(actualTableNameTwo);
        verify(freeMarkerEvaluatorForTables).evaluate("ddlForForm.ftl", tableDataOne);
        verify(martJdbcTemplate).execute("table one sql");
        verify(freeMarkerEvaluatorForTables, never()).evaluate("ddlForForm.ftl", tableDataTwo);
        verify(martJdbcTemplate, never()).execute("table two sql");
    }
}