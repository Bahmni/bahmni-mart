package org.bahmni.mart.config.job;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class JobDefinitionValidatorTest {
    @Mock
    private Logger logger;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private static final String GENERIC = "generic";

    @Before
    public void setUp() throws Exception {
        setValueForFinalStaticField(JobDefinitionValidator.class, "logger", logger);
    }

    @Test
    public void shouldThrowInvalidJobConfigurationExceptionIfReaderSQLAndReaderSQLFilePathAreEmpty() {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinition.getName()).thenReturn("test");
        when(jobDefinition.getTableName()).thenReturn("table");
        when(jobDefinition.getReaderSql()).thenReturn("");
        when(jobDefinition.getReaderSqlFilePath()).thenReturn("");
        when(jobDefinition.getType()).thenReturn(GENERIC);

        JobDefinitionValidator.validate(Arrays.asList(jobDefinition));

        verify(logger, times(1)).error("Reader SQL(or Reader SQL file path) is empty for the job 'test'");
    }

    @Test
    public void shouldThrowInvalidJobConfigurationExceptionIfReaderSQLAndReaderSQLFilePathAreNull() {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinition.getName()).thenReturn("test");
        when(jobDefinition.getTableName()).thenReturn("table");
        when(jobDefinition.getReaderSql()).thenReturn(null);
        when(jobDefinition.getReaderSqlFilePath()).thenReturn(null);
        when(jobDefinition.getType()).thenReturn(GENERIC);

        JobDefinitionValidator.validate(Arrays.asList(jobDefinition));

        verify(logger, times(1)).error("Reader SQL(or Reader SQL file path) is empty for the job 'test'");
    }

    @Test
    public void shouldThrowInvalidJobConfigurationExceptionIfTableIsEmpty() {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setReaderSql("select * from program");
        jobDefinition.setTableName("table");
        jobDefinition.setType(GENERIC);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setReaderSql("select * from program");
        jobDefinition1.setTableName("");
        jobDefinition1.setType(GENERIC);

        JobDefinitionValidator.validate(Arrays.asList(jobDefinition, jobDefinition1));
        verify(logger, times(1)).error("Table name is empty for the job 'test1'");

    }

    @Test
    public void shouldThrowInvalidJobConfigurationExceptionIfTableIsNull() {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setReaderSql("select * from program");
        jobDefinition.setTableName("table");
        jobDefinition.setType(GENERIC);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setReaderSql("select * from program");
        jobDefinition1.setTableName(null);
        jobDefinition1.setType(GENERIC);

        JobDefinitionValidator.validate(Arrays.asList(jobDefinition, jobDefinition1));
        verify(logger, times(1)).error("Table name is empty for the job 'test1'");


    }

    @Test
    public void shouldGiveTrueForValidJobConfigurations() {
        List<JobDefinition> jobDefinitions = new ArrayList<>();

        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinition.getName()).thenReturn("test");
        when(jobDefinition.getTableName()).thenReturn("table");
        when(jobDefinition.getReaderSql()).thenReturn("select * form program");
        when(jobDefinition.getType()).thenReturn(GENERIC);
        when(jobDefinition.getReaderSqlFilePath()).thenReturn("some file path");

        JobDefinition jobDefinition1 = mock(JobDefinition.class);
        when(jobDefinition1.getName()).thenReturn("test1");
        when(jobDefinition1.getTableName()).thenReturn("table1");
        when(jobDefinition1.getReaderSql()).thenReturn("select * form program1");
        when(jobDefinition1.getType()).thenReturn(GENERIC);
        when(jobDefinition1.getReaderSqlFilePath()).thenReturn("some file path1");

        JobDefinition jobDefinition2 = mock(JobDefinition.class);
        when(jobDefinition2.getName()).thenReturn("test2");
        when(jobDefinition2.getTableName()).thenReturn("table2");
        when(jobDefinition2.getReaderSql()).thenReturn("select * form program2");
        when(jobDefinition2.getType()).thenReturn(GENERIC);
        when(jobDefinition2.getReaderSqlFilePath()).thenReturn("some file path2");

        jobDefinitions.add(jobDefinition);
        jobDefinitions.add(jobDefinition1);
        jobDefinitions.add(jobDefinition2);

        assertTrue(JobDefinitionValidator.validate(jobDefinitions));
    }

    @Test
    public void shouldGiveTrueForNonEmptyReaderSQLFilePathAndEmptyReaderSQL() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinition.getName()).thenReturn("test1");
        when(jobDefinition.getTableName()).thenReturn("table1");
        when(jobDefinition.getReaderSql()).thenReturn("");
        when(jobDefinition.getReaderSqlFilePath()).thenReturn("some path to sql file");
        when(jobDefinition.getType()).thenReturn(GENERIC);

        assertTrue(JobDefinitionValidator.validate(Arrays.asList(jobDefinition)));
    }

    @Test
    public void shouldGiveFalseForDuplicateJobNameInJobConfigurations() {
        List<JobDefinition> jobDefinitions = new ArrayList<>();

        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setTableName("table");
        jobDefinition.setReaderSql("select * from program");
        jobDefinition.setType(GENERIC);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test");
        jobDefinition1.setTableName("table1");
        jobDefinition1.setReaderSql("select * from program1");
        jobDefinition1.setType(GENERIC);

        JobDefinition jobDefinition2 = new JobDefinition();
        jobDefinition2.setName("test2");
        jobDefinition2.setTableName("table2");
        jobDefinition2.setReaderSql("select * from program2");
        jobDefinition2.setType(GENERIC);

        jobDefinitions.add(jobDefinition2);
        jobDefinitions.add(jobDefinition1);
        jobDefinitions.add(jobDefinition);
        assertFalse(JobDefinitionValidator.validate(jobDefinitions));
    }

    @Test
    public void shouldGiveFalseForDuplicateTableNameInJobConfigurations() {
        List<JobDefinition> jobDefinitions = new ArrayList<>();

        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setTableName("table");
        jobDefinition.setReaderSql("select * from program");
        jobDefinition.setType(GENERIC);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setTableName("table1");
        jobDefinition1.setReaderSql("select * from program1");
        jobDefinition1.setType(GENERIC);

        JobDefinition jobDefinition2 = new JobDefinition();
        jobDefinition2.setName("test2");
        jobDefinition2.setTableName("table1");
        jobDefinition2.setReaderSql("select * from program2");
        jobDefinition2.setType(GENERIC);

        jobDefinitions.add(jobDefinition2);
        jobDefinitions.add(jobDefinition1);
        jobDefinitions.add(jobDefinition);
        assertFalse(JobDefinitionValidator.validate(jobDefinitions));
    }
}