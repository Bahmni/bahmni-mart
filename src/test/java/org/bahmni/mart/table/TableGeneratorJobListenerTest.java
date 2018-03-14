package org.bahmni.mart.table;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class TableGeneratorJobListenerTest {

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JdbcTemplate openMRSJdbc;

    @Mock
    private TableGeneratorStep tableGeneratorStep;

    private TableGeneratorJobListener tableGeneratorJobListener;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        tableGeneratorJobListener = new TableGeneratorJobListener();
        setValuesForMemberFields(tableGeneratorJobListener, "jobDefinitionReader", jobDefinitionReader);
        setValuesForMemberFields(tableGeneratorJobListener, "openMRSJdbc", openMRSJdbc);
        setValuesForMemberFields(tableGeneratorJobListener, "tableGeneratorStep", tableGeneratorStep);
    }

    @Test
    public void shouldCallCreateTablesOnProperJobExecution() {
        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);

        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(openMRSJdbc.query(any(String.class), any(TableDataExtractor.class))).thenReturn(new TableData());

        tableGeneratorJobListener.beforeJob(jobExecution);

        verify(jobExecution, times(1)).getJobInstance();
        verify(jobInstance, times(1)).getJobName();
        verify(tableGeneratorStep, times(1)).createTables(any());
    }

    @Test
    public void shouldStopJobWhenTableCreationFails() {
        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);
        doThrow(new BadSqlGrammarException("", "select from table",
                new SQLException())).when(tableGeneratorStep).createTables(any());
        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(openMRSJdbc.query(any(String.class), any(TableDataExtractor.class))).thenReturn(new TableData());

        tableGeneratorJobListener.beforeJob(jobExecution);

        verify(jobExecution, times(1)).stop();
    }

    @Test
    public void shouldReturnTableDataForMart() {
        String jobName = "test job";
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinition.getName()).thenReturn(jobName);
        String tableName = "test table";
        when(jobDefinition.getTableName()).thenReturn(tableName);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList(jobDefinition));
        TableData tableData = new TableData();
        TableColumn column = new TableColumn();
        column.setType("char");
        tableData.addColumn(column);
        when(openMRSJdbc.query(anyString(), any(TableDataExtractor.class))).thenReturn(tableData);

        TableData actualData = tableGeneratorJobListener.getTableDataForMart(jobName);

        List<TableColumn> columns = actualData.getColumns();

        assertEquals(tableName, actualData.getName());
        assertEquals(1, columns.size());
        assertEquals("text", columns.get(0).getType());
    }
}