package org.bahmni.mart.table.listener;

import org.bahmni.mart.config.job.EAVJobData;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
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

import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
    private EAVJobData eavJobData;

    @Mock
    private TableColumn tableColumn;

    private EAVJobListener eavJobListener;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        eavJobListener = new EAVJobListener();
        setValuesForSuperClassMemberFields(eavJobListener, "tableGeneratorStep", tableGeneratorStep);
        setValuesForSuperClassMemberFields(eavJobListener, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(eavJobListener, "openMRSJdbcTemplate", openMRSJdbcTemplate);
    }

    @Test
    public void shouldCallCreateTablesWithProperData() throws Exception {
        String attributeTable = "attributeTable";
        String primaryKey = "primaryKey";
        String sql = "select name from attributeTable;";

        TableColumn primaryColumn = new TableColumn("primaryKey", "integer", true, null);
        TableColumn localNameColumn = new TableColumn("givenLocalName", "text", false, null);
        TableColumn familyNameColumn = new TableColumn("familyName", "text", false, null);
        TableData tableData = new TableData();
        tableData.addAllColumns(Arrays.asList(primaryColumn, localNameColumn, familyNameColumn));

        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);

        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(jobDefinitionReader.getJobDefinitionByName(anyString())).thenReturn(jobDefinition);
        when(jobDefinition.getEavAttributes()).thenReturn(eavJobData);
        when(eavJobData.getAttributeTypeTableName()).thenReturn(attributeTable);
        when(openMRSJdbcTemplate.queryForList(sql, String.class))
                .thenReturn(Arrays.asList("givenLocalName", "familyName"));
        when(eavJobData.getPrimaryKey()).thenReturn(primaryKey);
        whenNew(TableColumn.class)
                .withArguments(primaryKey, "integer", true, null)
                .thenReturn(tableColumn);
        whenNew(TableColumn.class)
                .withArguments("givenLocalName", "text", false, null)
                .thenReturn(tableColumn);
        whenNew(TableColumn.class)
                .withArguments("familyName", "text", false, null)
                .thenReturn(tableColumn);
        doNothing().when(tableGeneratorStep).createTables(Arrays.asList(tableData));

        eavJobListener.beforeJob(jobExecution);

        verify(jobExecution, times(1)).getJobInstance();
        verify(jobInstance, times(1)).getJobName();
        verify(jobDefinitionReader, times(1)).getJobDefinitionByName(anyString());
        verify(eavJobData, times(1)).getAttributeTypeTableName();
        verify(openMRSJdbcTemplate, times(1)).queryForList(sql, String.class);
        verify(tableGeneratorStep, times(1)).createTables(any());
    }

    @Test
    public void shouldStopJobWhenTableCreationFails() {
        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);
        doThrow(new BadSqlGrammarException("", "select from table",
                new SQLException())).when(tableGeneratorStep).createTables(any());

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

}