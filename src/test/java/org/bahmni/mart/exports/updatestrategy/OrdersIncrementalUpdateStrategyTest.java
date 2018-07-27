package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.OrderStepConfigurer;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.SpecialCharacterResolver;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest(SpecialCharacterResolver.class)
@RunWith(PowerMockRunner.class)
public class OrdersIncrementalUpdateStrategyTest {

    private OrdersIncrementalUpdateStrategy ordersIncrementalUpdateStrategy;

    @Mock
    private OrderStepConfigurer orderStepConfigurer;

    @Mock
    private TableDataGenerator tableDataGenerator;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        ordersIncrementalUpdateStrategy = new OrdersIncrementalUpdateStrategy();

        setValuesForMemberFields(ordersIncrementalUpdateStrategy, "orderStepConfigurer", orderStepConfigurer);
        setValuesForMemberFields(ordersIncrementalUpdateStrategy, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(ordersIncrementalUpdateStrategy, "tableDataGenerator", tableDataGenerator);

        when(jobDefinitionReader.getJobDefinitionByName(anyString())).thenReturn(jobDefinition);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(mock(IncrementalUpdateConfig.class));
    }

    @Test
    public void shouldReturnTrueWhenJobDefinitionIsNull() throws Exception {
        when(jobDefinitionReader.getJobDefinitionByName("jobName")).thenReturn(null);

        assertTrue(ordersIncrementalUpdateStrategy.getMetaDataChangeStatus("table_name","jobName"));
    }

    @Test
    public void shouldReturnTrueWhenIncrementalUpdateConfigurationIsNull() throws Exception {
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(null);

        assertTrue(ordersIncrementalUpdateStrategy.getMetaDataChangeStatus("table_name","jobName"));
    }

    @Test
    public void shouldReturnFalseWhenThereIsNoMetadataChange() throws Exception {
        String tableName = "table_name";
        String jobName = "Orders Data";
        TableData tableData = new TableData(tableName);
        when(tableDataGenerator.getTableDataFromMart(eq(tableName), anyString())).thenReturn(tableData);
        when(orderStepConfigurer.getTableData(tableName)).thenReturn(tableData);

        boolean metaDataChangeStatus = ordersIncrementalUpdateStrategy.getMetaDataChangeStatus(tableName, jobName);

        verify(orderStepConfigurer).getTableData(tableName);
        verify(tableDataGenerator).getTableDataFromMart(eq(tableName), anyString());
        assertFalse(metaDataChangeStatus);
        verifyStatic();
        SpecialCharacterResolver.resolveTableData(tableData);
    }

    @Test
    public void shouldReturnTrueWhenThereIsAMetadataChange() throws Exception {
        String tableName = "table_name";
        String jobName = "Orders Data";
        TableData tableData = new TableData(tableName);
        when(tableDataGenerator.getTableDataFromMart(eq(tableName), anyString())).thenReturn(new TableData());
        when(orderStepConfigurer.getTableData(tableName)).thenReturn(tableData);

        boolean metaDataChangeStatus = ordersIncrementalUpdateStrategy.getMetaDataChangeStatus(tableName, jobName);

        verify(orderStepConfigurer).getTableData(tableName);
        verify(tableDataGenerator).getTableDataFromMart(eq(tableName), anyString());
        assertTrue(metaDataChangeStatus);
        verifyStatic();
        SpecialCharacterResolver.resolveTableData(tableData);
    }
}