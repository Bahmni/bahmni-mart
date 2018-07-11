package org.bahmni.mart.exports.writer;

import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.helper.incrementalupdate.AbstractIncrementalUpdater;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BaseWriterTest {

    private class SampleBaseWriter extends BaseWriter {

        @Override
        protected Set<String> getVoidedIds(List<?> items) {
            return null;
        }

    }

    @Mock
    private AbstractIncrementalUpdater incrementalUpdater;

    @Mock
    private TableData tableData;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private IncrementalUpdateConfig incrementalUpdateConfig;

    private List<Object> items = Arrays.asList();
    private BaseWriter baseWriter;
    private static final String KEY_NAME = "keyName";

    @Before
    public void setUp() {
        baseWriter = new SampleBaseWriter();
        baseWriter.setJobDefinition(jobDefinition);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(incrementalUpdateConfig);
        when(incrementalUpdater.isMetaDataChanged(anyString())).thenReturn(false);
    }

    @Test
    public void shouldNotDeleteVoidedRecordsIfJobDefinitionIsNull() {
        baseWriter.setJobDefinition(null);

        baseWriter.deletedVoidedRecords(items, incrementalUpdater, KEY_NAME, tableData);

        verify(incrementalUpdater, never()).isMetaDataChanged(KEY_NAME);
        verify(incrementalUpdater, never()).deleteVoidedRecords(anySet(), anyString(), anyString());
    }

    @Test
    public void shouldNotDeleteVoidedRecordsIfJobDefinitionDoesNotHaveIncrementalUpdateConfig() {
        baseWriter.setJobDefinition(jobDefinition);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(null);

        baseWriter.deletedVoidedRecords(items, incrementalUpdater, KEY_NAME, tableData);

        verify(incrementalUpdater, never()).isMetaDataChanged(KEY_NAME);
        verify(incrementalUpdater, never()).deleteVoidedRecords(anySet(), anyString(), anyString());
        verify(jobDefinition).getIncrementalUpdateConfig();
    }

    @Test
    public void shouldNotDeleteVoidedRecordsIfMetadataIsChanged() {
        when(incrementalUpdater.isMetaDataChanged(anyString())).thenReturn(true);

        baseWriter.deletedVoidedRecords(items, incrementalUpdater, KEY_NAME, tableData);

        verify(jobDefinition).getIncrementalUpdateConfig();
        verify(incrementalUpdater).isMetaDataChanged(KEY_NAME);
        verify(incrementalUpdater, never()).deleteVoidedRecords(anySet(), anyString(), anyString());
    }

    @Test
    public void shouldDeleteVoidedRecords() {
        String tableName = "tableName";
        String updateOn = "updateOn";

        when(tableData.getName()).thenReturn(tableName);
        when(incrementalUpdateConfig.getUpdateOn()).thenReturn(updateOn);

        baseWriter.deletedVoidedRecords(items, incrementalUpdater, KEY_NAME, tableData);

        verify(jobDefinition, atLeastOnce()).getIncrementalUpdateConfig();
        verify(incrementalUpdater).isMetaDataChanged(KEY_NAME);
        verify(incrementalUpdater).deleteVoidedRecords(anySet(), eq(tableName), eq(updateOn));
    }
}