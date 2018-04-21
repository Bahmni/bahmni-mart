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
    private static final String CUSTOM_SQL = "customSql";

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
        when(jobDefinition.getType()).thenReturn(CUSTOM_SQL);

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
        when(jobDefinition.getType()).thenReturn(CUSTOM_SQL);

        JobDefinitionValidator.validate(Arrays.asList(jobDefinition));

        verify(logger, times(1)).error("Reader SQL(or Reader SQL file path) is empty for the job 'test'");
    }

    @Test
    public void shouldThrowInvalidJobConfigurationExceptionIfTableIsEmpty() {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setReaderSql("select * from program");
        jobDefinition.setTableName("table");
        jobDefinition.setType(CUSTOM_SQL);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setReaderSql("select * from program");
        jobDefinition1.setTableName("");
        jobDefinition1.setType(CUSTOM_SQL);

        JobDefinitionValidator.validate(Arrays.asList(jobDefinition, jobDefinition1));
        verify(logger, times(1)).error("Table name is empty for the job 'test1'");

    }

    @Test
    public void shouldThrowInvalidJobConfigurationExceptionIfTableIsNull() {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setReaderSql("select * from program");
        jobDefinition.setTableName("table");
        jobDefinition.setType(CUSTOM_SQL);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setReaderSql("select * from program");
        jobDefinition1.setTableName(null);
        jobDefinition1.setType(CUSTOM_SQL);

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
        when(jobDefinition.getType()).thenReturn(CUSTOM_SQL);
        when(jobDefinition.getReaderSqlFilePath()).thenReturn("some file path");

        JobDefinition jobDefinition1 = mock(JobDefinition.class);
        when(jobDefinition1.getName()).thenReturn("test1");
        when(jobDefinition1.getTableName()).thenReturn("table1");
        when(jobDefinition1.getReaderSql()).thenReturn("select * form program1");
        when(jobDefinition1.getType()).thenReturn(CUSTOM_SQL);
        when(jobDefinition1.getReaderSqlFilePath()).thenReturn("some file path1");

        JobDefinition jobDefinition2 = mock(JobDefinition.class);
        when(jobDefinition2.getName()).thenReturn("test2");
        when(jobDefinition2.getTableName()).thenReturn("table2");
        when(jobDefinition2.getReaderSql()).thenReturn("select * form program2");
        when(jobDefinition2.getType()).thenReturn(CUSTOM_SQL);
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
        when(jobDefinition.getType()).thenReturn(CUSTOM_SQL);

        assertTrue(JobDefinitionValidator.validate(Arrays.asList(jobDefinition)));
    }

    @Test
    public void shouldGiveFalseForDuplicateJobNameInJobConfigurations() {
        List<JobDefinition> jobDefinitions = new ArrayList<>();

        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setTableName("table");
        jobDefinition.setReaderSql("select * from program");
        jobDefinition.setType(CUSTOM_SQL);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test");
        jobDefinition1.setTableName("table1");
        jobDefinition1.setReaderSql("select * from program1");
        jobDefinition1.setType(CUSTOM_SQL);

        JobDefinition jobDefinition2 = new JobDefinition();
        jobDefinition2.setName("test2");
        jobDefinition2.setTableName("table2");
        jobDefinition2.setReaderSql("select * from program2");
        jobDefinition2.setType(CUSTOM_SQL);

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
        jobDefinition.setType(CUSTOM_SQL);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setTableName("table1");
        jobDefinition1.setReaderSql("select * from program1");
        jobDefinition1.setType(CUSTOM_SQL);

        JobDefinition jobDefinition2 = new JobDefinition();
        jobDefinition2.setName("test2");
        jobDefinition2.setTableName("table1");
        jobDefinition2.setReaderSql("select * from program2");
        jobDefinition2.setType(CUSTOM_SQL);

        jobDefinitions.add(jobDefinition2);
        jobDefinitions.add(jobDefinition1);
        jobDefinitions.add(jobDefinition);
        assertFalse(JobDefinitionValidator.validate(jobDefinitions));
    }

    @Test
    public void shouldReturnFalseGivenEmptyCodeConfigsList() throws Exception {
        List<CodeConfig> codeConfigs = new ArrayList<>();

        assertFalse(JobDefinitionValidator.isValid(codeConfigs));
    }

    @Test
    public void shouldReturnTrueGivenAllNonNullAndNonEmptyFieldsInCodeConfig() throws Exception {
        CodeConfig codeConfig = mock(CodeConfig.class);
        when(codeConfig.getSource()).thenReturn("source");
        when(codeConfig.getType()).thenReturn("type");
        List<String> fields = Arrays.asList("field");
        when(codeConfig.getColumnsToCode()).thenReturn(fields);

        assertTrue(JobDefinitionValidator.isValid(Arrays.asList(codeConfig)));
    }

    @Test
    public void shouldReturnFalseGivenASourceAsNull() throws Exception {
        CodeConfig codeConfig = mock(CodeConfig.class);
        when(codeConfig.getType()).thenReturn("type");
        List<String> fields = Arrays.asList("field");
        when(codeConfig.getColumnsToCode()).thenReturn(fields);

        assertFalse(JobDefinitionValidator.isValid(Arrays.asList(codeConfig)));
    }

    @Test
    public void shouldReturnFalseGivenSourceTypeAsNull() throws Exception {
        CodeConfig codeConfig = mock(CodeConfig.class);
        when(codeConfig.getSource()).thenReturn("source");
        List<String> fields = Arrays.asList("field");
        when(codeConfig.getColumnsToCode()).thenReturn(fields);

        assertFalse(JobDefinitionValidator.isValid(Arrays.asList(codeConfig)));
    }

    @Test
    public void shouldReturnFalseGivenColumnsToCodeAsNull() throws Exception {
        CodeConfig codeConfig = mock(CodeConfig.class);
        when(codeConfig.getSource()).thenReturn("source");
        when(codeConfig.getType()).thenReturn("type");

        assertFalse(JobDefinitionValidator.isValid(Arrays.asList(codeConfig)));
    }

    @Test
    public void shouldReturnFalseGivenEmptyFields() throws Exception {
        CodeConfig codeConfig = null;

        assertFalse(JobDefinitionValidator.isValid(Arrays.asList(codeConfig)));
    }

    @Test
    public void shouldReturnFalseGivenEmptyConfigWithAllNullFeilds() throws Exception {
        CodeConfig codeConfig = new CodeConfig();

        assertFalse(JobDefinitionValidator.isValid(Arrays.asList(codeConfig)));
    }
}