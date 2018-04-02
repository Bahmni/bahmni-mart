package org.bahmni.mart.table.listener;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.table.TableDataExtractor;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQLByIgnoringColumns;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobDefinitionUtil.class)
public class TableGeneratorJobListenerTest {

    @Rule
    private ExpectedException expectedException = ExpectedException.none();

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private JdbcTemplate openMRSJdbcTemplate;

    @Mock
    private TableGeneratorStep tableGeneratorStep;

    private TableGeneratorJobListener tableGeneratorJobListener;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        mockStatic(JobDefinitionUtil.class);
        tableGeneratorJobListener = new TableGeneratorJobListener();
        setValuesForSuperClassMemberFields(tableGeneratorJobListener, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(tableGeneratorJobListener, "openMRSJdbcTemplate", openMRSJdbcTemplate);
        setValuesForSuperClassMemberFields(tableGeneratorJobListener, "tableGeneratorStep", tableGeneratorStep);

        when(getReaderSQLByIgnoringColumns(any(), anyString())).thenReturn("Some sql");
    }

    @Test
    public void shouldCallCreateTablesOnProperJobExecution() {
        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);

        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(jobDefinitionReader.getJobDefinitionByName(anyString())).thenReturn(jobDefinition);
        when(openMRSJdbcTemplate.query(any(String.class), any(TableDataExtractor.class))).thenReturn(new TableData());


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
        when(jobDefinitionReader.getJobDefinitionByName(anyString())).thenReturn(jobDefinition);
        when(openMRSJdbcTemplate.query(any(String.class), any(TableDataExtractor.class))).thenReturn(new TableData());

        tableGeneratorJobListener.beforeJob(jobExecution);

        verify(jobExecution, times(1)).stop();
    }

    @Test
    public void shouldReturnTableDataForMart() {
        String jobName = "test job";
        when(jobDefinition.getName()).thenReturn(jobName);
        String tableName = "test table";
        when(jobDefinition.getTableName()).thenReturn(tableName);
        when(jobDefinitionReader.getJobDefinitionByName(jobName)).thenReturn(jobDefinition);
        when(jobDefinition.getReaderSql()).thenReturn("dummy SQL");
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList(jobDefinition));

        TableData tableData = new TableData();
        tableData.addColumn(new TableColumn("test", "char", false, null));
        when(openMRSJdbcTemplate.query(anyString(), any(TableDataExtractor.class))).thenReturn(tableData);

        TableData actualData = tableGeneratorJobListener.getTableDataForMart(jobName);

        List<TableColumn> columns = actualData.getColumns();

        verify(jobDefinitionReader, times(1)).getJobDefinitionByName(jobName);
        verify(openMRSJdbcTemplate, times(1)).query(anyString(), any(TableDataExtractor.class));
        assertEquals(tableName, actualData.getName());
        assertEquals(1, columns.size());
        assertEquals("text", columns.get(0).getType());
    }

    @Test
    public void shouldThrowInvalidJobConfigurationExceptionIfSqqIsNotPresentForAJobDefinition() {
        String jobName = "testJob";
        when(jobDefinitionReader.getJobDefinitionByName(jobName)).thenReturn(jobDefinition);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Collections.emptyList());
        when(jobDefinition.getReaderSql()).thenReturn("some sql");
        when(jobDefinition.getName()).thenReturn(jobName);
        when(getReaderSQLByIgnoringColumns(any(), anyString())).thenReturn("");

        expectedException.expect(InvalidJobConfiguration.class);
        expectedException.expectMessage("Reader SQL is empty for the job definition 'testJob'");
        tableGeneratorJobListener.getTableDataForMart(jobName);
    }
}