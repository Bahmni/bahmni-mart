package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class EavIncrementalUpdateStrategyTest {

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private TableData existingTableData;

    @Mock
    private TableData tableData;

    @Mock
    private EAVJobListener eavJobListener;

    private EavIncrementalUpdateStrategy spyEavIncrementalUpdateStrategy;
    private String tableName;
    private String processedJobName;


    @Before
    public void setUp() throws Exception {
        EavIncrementalUpdateStrategy eavIncrementalUpdateStrategy = new EavIncrementalUpdateStrategy();
        setValuesForMemberFields(eavIncrementalUpdateStrategy, "jobDefinitionReader", jobDefinitionReader);
        setValuesForMemberFields(eavIncrementalUpdateStrategy, "eavJobListener", eavJobListener);
        this.spyEavIncrementalUpdateStrategy = spy(eavIncrementalUpdateStrategy);

        processedJobName = "processedJobName";
        String jobName = "job name";
        when(jobDefinition.getName()).thenReturn(jobName);
        tableName = "table name";
        when(jobDefinition.getTableName()).thenReturn(tableName);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(new IncrementalUpdateConfig());
        when(jobDefinitionReader.getJobDefinitionByProcessedName(processedJobName)).thenReturn(jobDefinition);
        when(eavJobListener.getTableDataForMart(jobName)).thenReturn(tableData);
    }

    @Test
    public void shouldReturnTrueIfMetadataChanged() {
        doReturn(existingTableData).when(spyEavIncrementalUpdateStrategy).getExistingTableData(tableName);

        boolean status = spyEavIncrementalUpdateStrategy.getMetaDataChangeStatus(processedJobName);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByProcessedName(processedJobName);
        verify(jobDefinition, atLeastOnce()).getName();
        verify(jobDefinition).getIncrementalUpdateConfig();
        verify(jobDefinition, atLeastOnce()).getTableName();
        verify(eavJobListener).getTableDataForMart("job name");
        verify(spyEavIncrementalUpdateStrategy).getExistingTableData(tableName);
    }

    @Test
    public void shouldReturnTrueIfJobDefinitionNameIsEmpty() {
        when(jobDefinition.getName()).thenReturn(null);

        boolean status = spyEavIncrementalUpdateStrategy.getMetaDataChangeStatus(processedJobName);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByProcessedName(processedJobName);
        verify(jobDefinition).getName();
        verify(jobDefinition, never()).getIncrementalUpdateConfig();
        verify(eavJobListener, never()).getTableDataForMart("job name");
        verify(spyEavIncrementalUpdateStrategy, never()).getExistingTableData(tableName);
    }

    @Test
    public void shouldReturnTrueIfIncrementalConfigIsEmpty() {
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(null);

        boolean status = spyEavIncrementalUpdateStrategy.getMetaDataChangeStatus(processedJobName);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByProcessedName(processedJobName);
        verify(jobDefinition).getName();
        verify(jobDefinition).getIncrementalUpdateConfig();
        verify(eavJobListener, never()).getTableDataForMart("job name");
        verify(spyEavIncrementalUpdateStrategy, never()).getExistingTableData(tableName);
    }
}