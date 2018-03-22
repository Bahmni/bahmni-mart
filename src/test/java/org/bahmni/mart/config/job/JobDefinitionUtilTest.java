package org.bahmni.mart.config.job;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JobDefinitionUtilTest {

    @Test
    public void shouldReturnSameReaderSQLWhenIgnoreColumnsAreNull() {
        String expectedReaderSQL = "select patient_program_id, program_id, patient_id, date_enrolled as `enrolled_on`" +
                "from patient_program ";

        String actualReaderSQL = JobDefinitionUtil
                .getReaderSQLByIgnoringColumns(null, expectedReaderSQL);
        assertEquals(expectedReaderSQL, actualReaderSQL);
    }

    @Test
    public void shouldReturnSameReaderSQLWhenIgnoreColumnsAreEmpty() {
        String expectedReaderSQL = "select patient_program_id, program_id, patient_id, date_enrolled as `enrolled_on`" +
                "from patient_program ";

        String actualReaderSQL = JobDefinitionUtil
                .getReaderSQLByIgnoringColumns(new ArrayList<>(), expectedReaderSQL);
        assertEquals(expectedReaderSQL, actualReaderSQL);
    }

    @Test
    public void shouldReturnReaderSqlByFilteringIgnoredColumns() {
        String readerSQL = "select patient_program_id, program_id as `programId`, p.patient_id, " +
                "p.date_enrolled as `enrolled_on`" +
                "from patient_program p";
        String expectedReaderSQL = "select patient_program_id from patient_program p";
        List<String> ignoreColumns = Arrays.asList("patient_id", "enrolled_on", "programId");

        assertEquals(expectedReaderSQL, JobDefinitionUtil.getReaderSQLByIgnoringColumns(ignoreColumns, readerSQL));
    }

    @Test
    public void shouldReturnEmptySQLIfAllTheColumnsAreIgnored() {
        String readerSQL = "select patient_program_id, program_id as `programId`, p.patient_id, " +
                "p.date_enrolled as `enrolled_on`" +
                "from patient_program p";
        List<String> ignoreColumns = Arrays.asList("patient_id", "enrolled_on", "programId", "patient_program_id");

        assertEquals("", JobDefinitionUtil.getReaderSQLByIgnoringColumns(ignoreColumns, readerSQL));
    }

    @Test
    public void shouldReturnNullIfReaderSqlIsNull() {
        assertNull(JobDefinitionUtil.getReaderSQLByIgnoringColumns(Arrays.asList(), null));
    }

    @Test
    public void shouldReturnEmptySqlIfReaderSqlIsEmpty() {
        assertEquals("", JobDefinitionUtil.getReaderSQLByIgnoringColumns(Arrays.asList(), ""));
    }
}