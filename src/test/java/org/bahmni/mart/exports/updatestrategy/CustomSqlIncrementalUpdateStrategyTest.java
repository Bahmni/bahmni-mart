package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
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

    private static final String JOB_NAME = "job name";
    private static final String TABLE_NAME = "table name";

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
    private String readerSql;


    @Before
    public void setUp() throws Exception {
        mockStatic(JobDefinitionUtil.class);

        CustomSqlIncrementalUpdateStrategy customSqlIncrementalUpdater = new CustomSqlIncrementalUpdateStrategy();
        setValuesForMemberFields(customSqlIncrementalUpdater, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(customSqlIncrementalUpdater, "tableDataGenerator", tableDataGenerator);
        spyCustomSqlIncrementalUpdater = spy(customSqlIncrementalUpdater);

        when(jobDefinition.getName()).thenReturn(JOB_NAME);
        when(jobDefinition.getTableName()).thenReturn(TABLE_NAME);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(new IncrementalUpdateConfig());
        readerSql = "select * from table";
        when(JobDefinitionUtil.getReaderSQL(jobDefinition)).thenReturn(readerSql);
        when(tableDataGenerator.getTableDataFromOpenmrs(TABLE_NAME, readerSql)).thenReturn(tableData);
        when(jobDefinitionReader.getJobDefinitionByName(JOB_NAME)).thenReturn(jobDefinition);
    }

    @Test
    public void shouldReturnTrueIfMetadataChanged() {
        doReturn(existingTableData).when(spyCustomSqlIncrementalUpdater).getExistingTableData(TABLE_NAME);

        boolean status = spyCustomSqlIncrementalUpdater.getMetaDataChangeStatus(TABLE_NAME, JOB_NAME);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByName(JOB_NAME);
        verify(jobDefinition).getName();
        verify(jobDefinition).getIncrementalUpdateConfig();
        verify(spyCustomSqlIncrementalUpdater).getExistingTableData(TABLE_NAME);
        verifyStatic();
        JobDefinitionUtil.getReaderSQL(jobDefinition);
        verify(tableDataGenerator).getTableDataFromOpenmrs(TABLE_NAME, readerSql);
    }

    @Test
    public void shouldReturnFalseIfMetadataIsSame() {
        doReturn(tableData).when(spyCustomSqlIncrementalUpdater).getExistingTableData(TABLE_NAME);

        boolean status = spyCustomSqlIncrementalUpdater.getMetaDataChangeStatus(TABLE_NAME, JOB_NAME);

        assertFalse(status);
        verify(jobDefinitionReader).getJobDefinitionByName(JOB_NAME);
        verify(jobDefinition).getName();
        verify(jobDefinition).getIncrementalUpdateConfig();
        verify(spyCustomSqlIncrementalUpdater).getExistingTableData(TABLE_NAME);
        verifyStatic();
        JobDefinitionUtil.getReaderSQL(jobDefinition);
        verify(tableDataGenerator).getTableDataFromOpenmrs(TABLE_NAME, readerSql);
    }

    @Test
    public void shouldReturnTrueIfJobDefinitionNameIsEmpty() {
        when(jobDefinition.getName()).thenReturn("");

        boolean status = spyCustomSqlIncrementalUpdater.getMetaDataChangeStatus(TABLE_NAME, JOB_NAME);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByName(JOB_NAME);
        verify(jobDefinition).getName();
        verify(jobDefinition, never()).getIncrementalUpdateConfig();
        verify(spyCustomSqlIncrementalUpdater, never()).getExistingTableData(TABLE_NAME);
        verifyStatic(never());
        JobDefinitionUtil.getReaderSQL(jobDefinition);
        verify(tableDataGenerator, never()).getTableDataFromOpenmrs(any(), any());
    }

    @Test
    public void shouldReturnTrueIfIncrementalUpdateConfigIsNull() {
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(null);

        boolean status = spyCustomSqlIncrementalUpdater.getMetaDataChangeStatus(TABLE_NAME, JOB_NAME);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByName(JOB_NAME);
        verify(jobDefinition).getName();
        verify(jobDefinition).getIncrementalUpdateConfig();
        verify(spyCustomSqlIncrementalUpdater, never()).getExistingTableData(TABLE_NAME);
        verifyStatic(never());
        JobDefinitionUtil.getReaderSQL(jobDefinition);
        verify(tableDataGenerator, never()).getTableDataFromOpenmrs(any(), any());
    }
}