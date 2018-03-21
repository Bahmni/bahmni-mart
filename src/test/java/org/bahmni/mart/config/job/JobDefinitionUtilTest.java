package org.bahmni.mart.config.job;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobDefinitionUtilTest {

    @Test
    public void shouldReturnSameReaderSQLWhenIgnoreColumnsAreNull() {
        JobDefinition jobDefinition = mock(JobDefinition.class);

        String expectedReaderSQL = "select patient_program_id, program_id, patient_id, date_enrolled as `enrolled_on`" +
                "from patient_program ";
        when(jobDefinition.getReaderSql()).thenReturn(expectedReaderSQL);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(null);

        String actualReaderSQL = JobDefinitionUtil.getReaderSQLByIgnoringColumns(jobDefinition);
        Assert.assertEquals(expectedReaderSQL, actualReaderSQL);
    }

    @Test
    public void shouldReturnSameReaderSQLWhenIgnoreColumnsAreEmpty() {
        JobDefinition jobDefinition = mock(JobDefinition.class);

        String expectedReaderSQL = "select patient_program_id, program_id, patient_id, date_enrolled as `enrolled_on`" +
                "from patient_program ";
        when(jobDefinition.getReaderSql()).thenReturn(expectedReaderSQL);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(new ArrayList<>());

        String actualReaderSQL = JobDefinitionUtil.getReaderSQLByIgnoringColumns(jobDefinition);
        Assert.assertEquals(expectedReaderSQL, actualReaderSQL);
    }

    @Test
    public void shouldReturnReaderSqlByFilteringIgnoredColumns() {
        JobDefinition jobDefinition = mock(JobDefinition.class);

        String readerSQL = "select patient_program_id, program_id as `programId`, p.patient_id, " +
                "p.date_enrolled as `enrolled_on`" +
                "from patient_program p";
        String expectedReaderSQL = "select patient_program_id from patient_program p";
        when(jobDefinition.getReaderSql()).thenReturn(readerSQL);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Arrays.asList("patient_id", "enrolled_on", "programId"));

        String actualReaderSQL = JobDefinitionUtil.getReaderSQLByIgnoringColumns(jobDefinition);

        Assert.assertEquals(expectedReaderSQL, actualReaderSQL);

    }

    @Test
    public void shouldReturnEmptySQLIfAllTheColumnsAreIgnored() {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        String readerSQL = "select patient_program_id, program_id as `programId`, p.patient_id, " +
                "p.date_enrolled as `enrolled_on`" +
                "from patient_program p";
        String expectedReaderSQL = "";

        when(jobDefinition.getReaderSql()).thenReturn(readerSQL);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Arrays.asList("patient_id", "enrolled_on",
                "programId", "patient_program_id"));

        String actualReaderSQL = JobDefinitionUtil.getReaderSQLByIgnoringColumns(jobDefinition);

        Assert.assertEquals(expectedReaderSQL, actualReaderSQL);
    }

    @Test
    public void shouldReturnNullIfReaderSqlIsNull() {
        JobDefinition jobDefinition = mock(JobDefinition.class);

        when(jobDefinition.getReaderSql()).thenReturn(null);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Arrays.asList());

        String actualReaderSQL = JobDefinitionUtil.getReaderSQLByIgnoringColumns(jobDefinition);
        Assert.assertNull(actualReaderSQL);
    }

    @Test
    public void shouldReturnEmptySqlIfReaderSqlIsEmpty() {
        JobDefinition jobDefinition = mock(JobDefinition.class);

        when(jobDefinition.getReaderSql()).thenReturn("");
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Arrays.asList());

        String actualReaderSQL = JobDefinitionUtil.getReaderSQLByIgnoringColumns(jobDefinition);
        Assert.assertEquals("", actualReaderSQL);
    }
}