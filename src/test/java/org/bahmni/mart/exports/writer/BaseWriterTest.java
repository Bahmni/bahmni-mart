//package org.bahmni.mart.exports.writer;
//
//import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
//import org.bahmni.mart.config.job.model.JobDefinition;
//import org.bahmni.mart.exports.updatestrategy.AbstractIncrementalUpdateStrategy;
//import org.bahmni.mart.table.domain.TableData;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Matchers.anySet;
//import static org.mockito.Matchers.anyString;
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.atLeastOnce;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@RunWith(PowerMockRunner.class)
//public class BaseWriterTest {
//
//    private static final String TABLE_NAME = "tableName";
//    private static final String JOB_NAME = "jobName";
//
//    private class SampleBaseWriter extends BaseWriter {
//
//        @Override
//        protected Set<String> getVoidedIds(List<?> items) {
//            return new HashSet<>(Arrays.asList("3", "4", "5"));
//        }
//
//    }
//
//    @Mock
//    private AbstractIncrementalUpdateStrategy incrementalUpdater;
//
//    @Mock
//    private TableData tableData;
//
//    @Mock
//    private JobDefinition jobDefinition;
//
//    @Mock
//    private IncrementalUpdateConfig incrementalUpdateConfig;
//
//    private List<Object> items = Arrays.asList();
//    private BaseWriter baseWriter;
//
//    @Before
//    public void setUp() {
//        baseWriter = new SampleBaseWriter();
//        baseWriter.setJobDefinition(jobDefinition);
//        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(incrementalUpdateConfig);
//        when(incrementalUpdater.isMetaDataChanged(anyString(), anyString())).thenReturn(false);
//        when(tableData.getName()).thenReturn(TABLE_NAME);
//        when(jobDefinition.getName()).thenReturn(JOB_NAME);
//    }
//
//    @Test
//    public void shouldNotDeleteVoidedRecordsIfJobDefinitionIsNull() {
//        baseWriter.setJobDefinition(null);
//
//        baseWriter.deletedVoidedRecords(items, incrementalUpdater, tableData);
//
//        verify(incrementalUpdater, never()).isMetaDataChanged(anyString(), anyString());
//        verify(incrementalUpdater, never()).deleteVoidedRecords(anySet(), anyString(), anyString());
//    }
//
//    @Test
//    public void shouldNotDeleteVoidedRecordsIfJobDefinitionDoesNotHaveIncrementalUpdateConfig() {
//        baseWriter.setJobDefinition(jobDefinition);
//        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(null);
//
//        baseWriter.deletedVoidedRecords(items, incrementalUpdater, tableData);
//
//        verify(incrementalUpdater, never()).isMetaDataChanged(anyString(), anyString());
//        verify(incrementalUpdater, never()).deleteVoidedRecords(anySet(), anyString(), anyString());
//        verify(jobDefinition).getIncrementalUpdateConfig();
//    }
//
//    @Test
//    public void shouldNotDeleteVoidedRecordsIfMetadataIsChanged() {
//        when(incrementalUpdater.isMetaDataChanged(anyString(), anyString())).thenReturn(true);
//
//        baseWriter.deletedVoidedRecords(items, incrementalUpdater, tableData);
//
//        verify(jobDefinition).getIncrementalUpdateConfig();
//        verify(incrementalUpdater).isMetaDataChanged(TABLE_NAME, JOB_NAME);
//        verify(incrementalUpdater, never()).deleteVoidedRecords(anySet(), anyString(), anyString());
//    }
//
//    @Test
//    public void shouldDeleteVoidedRecords() {
//        String updateOn = "updateOn";
//        when(incrementalUpdateConfig.getUpdateOn()).thenReturn(updateOn);
//
//        baseWriter.deletedVoidedRecords(items, incrementalUpdater, tableData);
//
//        verify(jobDefinition, atLeastOnce()).getIncrementalUpdateConfig();
//        verify(incrementalUpdater).isMetaDataChanged(TABLE_NAME, JOB_NAME);
//        verify(incrementalUpdater)
//                .deleteVoidedRecords(eq(new HashSet<>(Arrays.asList("3", "4", "5"))), eq(TABLE_NAME), eq(updateOn));
//    }
//
//    @Test
//    public void shouldNotDeleteRecordsFromPreviousChunkWhenSameIdIsPresentAcrossChunks() throws Exception {
//        String updateOn = "updateOn";
//
//        when(incrementalUpdateConfig.getUpdateOn()).thenReturn(updateOn);
//
//        setValuesForSuperClassMemberFields(baseWriter, "processedIds", new HashSet<>(Arrays.asList("1", "2", "3")));
//
//        baseWriter.deletedVoidedRecords(items, incrementalUpdater, tableData);
//        verify(jobDefinition, atLeastOnce()).getIncrementalUpdateConfig();
//        verify(incrementalUpdater).isMetaDataChanged(TABLE_NAME, JOB_NAME);
//        HashSet<String> expectedProcessedIds = new HashSet<>(Arrays.asList("4", "5"));
//        verify(incrementalUpdater).deleteVoidedRecords(eq(expectedProcessedIds), eq(TABLE_NAME), eq(updateOn));
//        assertEquals(new HashSet<>(Arrays.asList("1", "2", "3", "4", "5")), baseWriter.getProcessedIds());
//    }
//}