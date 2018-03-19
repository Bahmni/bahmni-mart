package org.bahmni.mart.config.job;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobDefinitionUtilTest {

    @Test
    public void shouldReturnSameReaderSQLWhenThereAreNoIgnoreColumns() {
        JobDefinition jobDefinition = mock(JobDefinition.class);

        String expectedReaderSQL = "select patient_program_id, program_id, patient_id, date_enrolled as `enrolled_on`" +
                "from patient_program ";
        when(jobDefinition.getReaderSql()).thenReturn(expectedReaderSQL);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(null);

        String actualReaderSQL = JobDefinitionUtil.getReaderSQLByIgnoringColumns(jobDefinition);
        Assert.assertEquals(expectedReaderSQL, actualReaderSQL);
    }

    @Test
    public void shouldReturnReaderSqlByFilteringIgnored() {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        JobDefinition.ColumnsToIgnore columnsToIgnore = mock(JobDefinition.ColumnsToIgnore.class);
        JobDefinition.ColumnsToIgnore columnsToIgnore1 = mock(JobDefinition.ColumnsToIgnore.class);

        String readerSQL = "select patient_program_id, program_id, patient_id, date_enrolled as `enrolled_on`" +
                "from patient_program ";
        String expectedReaderSQL = "select patient_program_id from patient_program ";
        when(jobDefinition.getReaderSql()).thenReturn(readerSQL);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Arrays.asList(columnsToIgnore, columnsToIgnore1));
        when(columnsToIgnore.getColumns()).thenReturn(Arrays.asList("patient_id", "enrolled_on"));
        when(columnsToIgnore1.getColumns()).thenReturn(Arrays.asList("program_id"));

        String actualReaderSQL = JobDefinitionUtil.getReaderSQLByIgnoringColumns(jobDefinition);

        Assert.assertEquals(expectedReaderSQL, actualReaderSQL);

    }
}