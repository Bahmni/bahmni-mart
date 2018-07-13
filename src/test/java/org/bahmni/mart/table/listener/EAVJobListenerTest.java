package org.bahmni.mart.table.listener;

import org.bahmni.mart.config.job.model.EavAttributes;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.table.CodesProcessor;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
public class EAVJobListenerTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private TableGeneratorStep tableGeneratorStep;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private JdbcTemplate openMRSJdbcTemplate;

    @Mock
    private EavAttributes eavAttributes;

    @Mock
    private TableColumn tableColumn;

    @Mock
    private CodesProcessor codesProcessor;

    private EAVJobListener eavJobListener;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        eavJobListener = new EAVJobListener();
        setValuesForSuperClassMemberFields(eavJobListener, "tableGeneratorStep", tableGeneratorStep);
        setValuesForSuperClassMemberFields(eavJobListener, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(eavJobListener, "openMRSJdbcTemplate", openMRSJdbcTemplate);
    }

    @Test
    public void shouldCallCreateTablesWithAllTableColumnsWhenIgnoredColumnIsNull() throws Exception {
        String sql = "select name from attributeTable;";
        String jobName = "JobName";
        setUpMocks(sql, null);

        TableData tableData = eavJobListener.getTableDataForMart(jobName);

        verifyMethodCalls(jobName, sql);
        verify(jobDefinition, times(1)).getColumnsToIgnore();
        assertEquals(3, tableData.getColumns().size());
        List<String> columnNames = tableData.getColumns().stream()
                .map(TableColumn::getName).collect(Collectors.toList());
        assertTrue(columnNames.containsAll(Arrays.asList("primaryKey", "givenLocalName", "familyName")));
    }

    @Test
    public void shouldCallCreateTablesWithAllTableColumnsWhenIgnoredColumnIsEmpty() throws Exception {
        String sql = "select name from attributeTable;";
        String jobName = "JobName";

        setUpMocks(sql, Collections.emptyList());

        TableData tableData = eavJobListener.getTableDataForMart(jobName);
        verifyMethodCalls(jobName, sql);

        verify(jobDefinition, times(1)).getColumnsToIgnore();
        assertEquals(3, tableData.getColumns().size());
        List<String> columnNames = tableData.getColumns().stream()
                .map(TableColumn::getName).collect(Collectors.toList());
        assertTrue(columnNames.containsAll(Arrays.asList("primaryKey", "givenLocalName", "familyName")));
    }

    @Test
    public void shouldGetTableDataForGivenJobDefinition() throws Exception {
        setUpMocks("select name from attributeTable;", Collections.emptyList());

        TableData tableData = eavJobListener.getTableDataForMart(jobDefinition);

        verify(jobDefinitionReader, never()).getJobDefinitionByName(anyString());
        assertEquals(3, tableData.getColumns().size());
    }

    @Test
    public void shouldCallCreateTablesWithOutIgnoreColumns() throws Exception {
        String sql = "select name from attributeTable;";
        String jobName = "jobName";
        setUpMocks(sql, Arrays.asList("givenLocalName"));

        TableData tableData = eavJobListener.getTableDataForMart(jobName);
        verifyMethodCalls(jobName, sql);

        verify(jobDefinition, times(1)).getColumnsToIgnore();
        assertEquals(2, tableData.getColumns().size());
        List<String> columnNames = tableData.getColumns().stream()
                .map(TableColumn::getName).collect(Collectors.toList());
        assertTrue(columnNames.containsAll(Arrays.asList("primaryKey", "familyName")));
    }

    private void verifyMethodCalls(String jobName, String sql) {
        verify(jobDefinitionReader, times(1)).getJobDefinitionByName(anyString());
        verify(eavAttributes, times(1)).getAttributeTypeTableName();
        verify(openMRSJdbcTemplate, times(1)).queryForList(sql, String.class);
        verify(jobDefinitionReader, times(1)).getJobDefinitionByName(jobName);
    }

    private void setUpMocks(String sql, List<String> ignoreColumns) throws Exception {
        String attributeTable = "attributeTable";
        String primaryKey = "primaryKey";

        when(jobDefinitionReader.getJobDefinitionByName(anyString())).thenReturn(jobDefinition);
        when(jobDefinition.getEavAttributes()).thenReturn(eavAttributes);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(ignoreColumns);
        when(eavAttributes.getAttributeTypeTableName()).thenReturn(attributeTable);
        when(openMRSJdbcTemplate.queryForList(sql, String.class))
                .thenReturn(Arrays.asList("givenLocalName", "familyName"));
        when(eavAttributes.getPrimaryKey()).thenReturn(primaryKey);
        whenNew(TableColumn.class)
                .withArguments(primaryKey, "integer", true, null)
                .thenReturn(tableColumn);
        whenNew(TableColumn.class)
                .withArguments("givenLocalName", "text", false, null)
                .thenReturn(tableColumn);
        whenNew(TableColumn.class)
                .withArguments("familyName", "text", false, null)
                .thenReturn(tableColumn);
    }

    @Test
    public void shouldStopJobWhenTableCreationFails() {
        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);
        doThrow(new BadSqlGrammarException("", "select from table", new SQLException()))
                .when(tableGeneratorStep).createTables(anyListOf(TableData.class), any(JobDefinition.class));

        when(jobExecution.getJobInstance()).thenReturn(jobInstance);

        eavJobListener.beforeJob(jobExecution);

        verify(jobExecution, times(1)).stop();
    }

    @Test
    public void shouldStopJobWhenThereIsNoJobDefinitionForGivenName() {
        JobExecution jobExecution = mock(JobExecution.class);

        when(jobDefinitionReader.getJobDefinitionByName("jobName")).thenReturn(null);
        when(jobExecution.getJobInstance()).thenReturn(mock(JobInstance.class));

        eavJobListener.beforeJob(jobExecution);

        verify(jobExecution, times(1)).stop();
    }

    @Test
    public void shouldSetUpCodesDataWhenCodeProcessorIsNotNull() throws Exception {
        setValuesForMemberFields(eavJobListener, "codesProcessor", codesProcessor);

        eavJobListener.setUpPreProcessorData();

        verify(codesProcessor, times(1)).setUpCodesData();
    }

    @Test
    public void shouldNotSetUpCodesDataGivenNullAsCodeProcessor() {
        eavJobListener.setUpPreProcessorData();

        verify(codesProcessor, never()).setUpCodesData();
    }
}