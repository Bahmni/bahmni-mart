package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest(SpecialCharacterResolver.class)
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
    private static final String TABLE_NAME = "table name";
    private static final String JOB_NAME = "job name";

    @Before
    public void setUp() throws Exception {
        EavIncrementalUpdateStrategy eavIncrementalUpdateStrategy = new EavIncrementalUpdateStrategy();
        setValuesForMemberFields(eavIncrementalUpdateStrategy, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(eavIncrementalUpdateStrategy, "listener", eavJobListener);

        mockStatic(SpecialCharacterResolver.class);
        this.spyEavIncrementalUpdateStrategy = spy(eavIncrementalUpdateStrategy);

        when(jobDefinition.getName()).thenReturn(JOB_NAME);
        when(jobDefinition.getTableName()).thenReturn(TABLE_NAME);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(new IncrementalUpdateConfig());
        when(jobDefinitionReader.getJobDefinitionByName(JOB_NAME)).thenReturn(jobDefinition);
        when(eavJobListener.getTableDataForMart(JOB_NAME)).thenReturn(tableData);
    }

    @Test
    public void shouldReturnTrueIfMetadataChanged() {
        doReturn(existingTableData).when(spyEavIncrementalUpdateStrategy).getExistingTableData(TABLE_NAME);

        boolean status = spyEavIncrementalUpdateStrategy.getMetaDataChangeStatus(TABLE_NAME, JOB_NAME);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByName(JOB_NAME);
        verify(jobDefinition, atLeastOnce()).getName();
        verify(jobDefinition).getIncrementalUpdateConfig();
        verify(jobDefinition, atLeastOnce()).getTableName();
        verify(eavJobListener).getTableDataForMart("job name");
        verify(spyEavIncrementalUpdateStrategy).getExistingTableData(TABLE_NAME);
        verifyStatic();
        SpecialCharacterResolver.resolveTableData(tableData);
    }

    @Test
    public void shouldReturnTrueIfJobDefinitionNameIsEmpty() {
        when(jobDefinition.getName()).thenReturn(null);

        boolean status = spyEavIncrementalUpdateStrategy.getMetaDataChangeStatus(TABLE_NAME, JOB_NAME);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByName(JOB_NAME);
        verify(jobDefinition).getName();
        verify(jobDefinition, never()).getIncrementalUpdateConfig();
        verify(eavJobListener, never()).getTableDataForMart("job name");
        verify(spyEavIncrementalUpdateStrategy, never()).getExistingTableData(TABLE_NAME);
    }

    @Test
    public void shouldReturnTrueIfIncrementalConfigIsEmpty() {
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(null);

        boolean status = spyEavIncrementalUpdateStrategy.getMetaDataChangeStatus(TABLE_NAME, JOB_NAME);

        assertTrue(status);
        verify(jobDefinitionReader).getJobDefinitionByName(JOB_NAME);
        verify(jobDefinition).getName();
        verify(jobDefinition).getIncrementalUpdateConfig();
        verify(eavJobListener, never()).getTableDataForMart("job name");
        verify(spyEavIncrementalUpdateStrategy, never()).getExistingTableData(TABLE_NAME);
    }
}