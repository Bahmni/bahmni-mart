package org.bahmni.mart.config.job;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQLByIgnoringColumns;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JobDefinitionUtilTest {

    @Test
    public void shouldReturnSameReaderSQLWhenIgnoreColumnsAreNull() {
        String expectedReaderSQL = "select patient_program_id, program_id, patient_id, date_enrolled as `enrolled_on`" +
                "from patient_program ";

        String actualReaderSQL =
                getReaderSQLByIgnoringColumns(null, expectedReaderSQL);
        assertEquals(expectedReaderSQL, actualReaderSQL);
    }

    @Test
    public void shouldReturnSameReaderSQLWhenIgnoreColumnsAreEmpty() {
        String expectedReaderSQL = "select patient_program_id, program_id, patient_id, date_enrolled as `enrolled_on`" +
                "from patient_program ";

        String actualReaderSQL =
                getReaderSQLByIgnoringColumns(new ArrayList<>(), expectedReaderSQL);
        assertEquals(expectedReaderSQL, actualReaderSQL);
    }

    @Test
    public void shouldReturnReaderSqlByFilteringIgnoredColumns() {
        String readerSQL = "select patient_program_id, program_id as `programId`, p.patient_id, " +
                "p.date_enrolled as `enrolled_on`" +
                "from patient_program p";
        String expectedReaderSQL = "select patient_program_id from patient_program p";
        List<String> ignoreColumns = Arrays.asList("patient_id", "enrolled_on", "programId");

        assertEquals(expectedReaderSQL, getReaderSQLByIgnoringColumns(ignoreColumns, readerSQL));
    }

    @Test
    public void shouldReturnReaderSqlByFilteringIgnoredColumn() {
        String readerSQL = "select patient_program_id, program_id as `programId`, p.patient_id, " +
                "p.date_enrolled as `enrolled_on`" +
                "from patient_program p";
        String expectedReaderSQL = "select patient_program_id, program_id as `programId`, " +
                "p.date_enrolled as `enrolled_on` from patient_program p";

        assertEquals(expectedReaderSQL, getReaderSQLByIgnoringColumns(Arrays.asList("patient_id"), readerSQL));
    }

    @Test
    public void shouldReturnEmptySQLIfAllTheColumnsAreIgnored() {
        String readerSQL = "select patient_program_id, program_id as `programId`, p.patient_id, " +
                "p.date_enrolled as `enrolled_on`" +
                "from patient_program p";
        List<String> ignoreColumns = Arrays.asList("patient_id", "enrolled_on", "programId", "patient_program_id");

        assertEquals("", getReaderSQLByIgnoringColumns(ignoreColumns, readerSQL));
    }

    @Test
    public void shouldReturnNullIfReaderSqlIsNull() {
        assertNull(getReaderSQLByIgnoringColumns(Arrays.asList(), null));
    }

    @Test
    public void shouldReturnEmptySqlIfReaderSqlIsEmpty() {
        assertEquals("", getReaderSQLByIgnoringColumns(Arrays.asList(), ""));
    }
}