package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

// NOT WRITING MORE TESTS AS COMMON METHODS ARE COVERED IN ObsIncrementalUpdateStrategyTest.class

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobDefinitionUtil.class)
public class CustomSqlIncrementalUpdateStrategyTest {

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private TableData existingTableData;

    @Mock
    private TableData tableData;

    @Mock
    private TableDataGenerator tableDataGenerator;

    private CustomSqlIncrementalUpdateStrategy spyCustomSqlIncrementalUpdater;
    private String tableName;
    private String readerSql;
    private String processedJobName;


    @Before
    public void setUp() throws Exception {
        mockStatic(JobDefinitionUtil.class);

        CustomSqlIncrementalUpdateStrategy customSqlIncrementalUpdater = new CustomSqlIncrementalUpdateStrategy();
        setValuesForMemberFields(customSqlIncrementalUpdater, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(customSqlIncrementalUpdater, "tableDataGenerator", tableDataGenerator);
        spyCustomSqlIncrementalUpdater = spy(customSqlIncrementalUpdater);

        processedJobName = "processedJobName";
        when(jobDefinition.getName()).thenReturn("job name");
        tableName = "table name";
        when(jobDefinition.getTableName()).thenReturn(tableName);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(new IncrementalUpdateConfig());
        readerSql = "select * from table";
        when(JobDefinitionUtil.getReaderSQL(jobDefinition)).thenReturn(readerSql);
        when(tableDataGenerator.getTableData(tableName, readerSql)).thenReturn(tableData);
        when(jobDefinitionReader.getJobDefinitionByProcessedName(processedJobName)).thenReturn(jobDefinition);
    }

    @Test
    public void shouldReturnTrueIfMetadataChanged() {
        doReturn(existingTableData).when(spyCustomSqlIncrementalUpdater).getExistingTableData(tableName);

        boolean status = spyCustomSqlIncrementalUpdater.getMetaDataChangeStatus(processedJobName);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByProcessedName(processedJobName);
        verify(jobDefinition).getName();
        verify(jobDefinition).getTableName();
        verify(spyCustomSqlIncrementalUpdater).getExistingTableData(tableName);
        verifyStatic();
        JobDefinitionUtil.getReaderSQL(jobDefinition);
        verify(tableDataGenerator).getTableData(tableName, readerSql);
    }

    @Test
    public void shouldReturnFalseIfMetadataIsSame() {
        doReturn(tableData).when(spyCustomSqlIncrementalUpdater).getExistingTableData(tableName);

        boolean status = spyCustomSqlIncrementalUpdater.getMetaDataChangeStatus(processedJobName);

        assertFalse(status);
        verify(jobDefinitionReader).getJobDefinitionByProcessedName(processedJobName);
        verify(jobDefinition).getName();
        verify(jobDefinition).getTableName();
        verify(spyCustomSqlIncrementalUpdater).getExistingTableData(tableName);
        verifyStatic();
        JobDefinitionUtil.getReaderSQL(jobDefinition);
        verify(tableDataGenerator).getTableData(tableName, readerSql);
    }

    @Test
    public void shouldReturnTrueIfJobDefinitionNameIsEmpty() {
        when(jobDefinition.getName()).thenReturn("");

        boolean status = spyCustomSqlIncrementalUpdater.getMetaDataChangeStatus(processedJobName);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByProcessedName(processedJobName);
        verify(jobDefinition).getName();
        verify(jobDefinition, never()).getTableName();
        verify(spyCustomSqlIncrementalUpdater, never()).getExistingTableData(tableName);
        verifyStatic(never());
        JobDefinitionUtil.getReaderSQL(jobDefinition);
        verify(tableDataGenerator, never()).getTableData(any(), any());
    }

    @Test
    public void shouldReturnTrueIfIncrementalUpdateConfigIsNull() {
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(null);

        boolean status = spyCustomSqlIncrementalUpdater.getMetaDataChangeStatus(processedJobName);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByProcessedName(processedJobName);
        verify(jobDefinition).getName();
        verify(jobDefinition, never()).getTableName();
        verify(spyCustomSqlIncrementalUpdater, never()).getExistingTableData(tableName);
        verifyStatic(never());
        JobDefinitionUtil.getReaderSQL(jobDefinition);
        verify(tableDataGenerator, never()).getTableData(any(), any());
    }
}