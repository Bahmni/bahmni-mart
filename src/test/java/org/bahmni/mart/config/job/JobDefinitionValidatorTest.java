package org.bahmni.mart.config.job;

import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JobDefinitionValidatorTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private static final String GENERIC = "generic";

    @Test
    public void shouldThrowInvalidJobConfigurationExceptionIfReaderSQLIsEmpty() {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setReaderSql("select * from program");
        jobDefinition.setTableName("table");
        jobDefinition.setType(GENERIC);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setReaderSql("");
        jobDefinition1.setTableName("table1");
        jobDefinition1.setType(GENERIC);

        List<JobDefinition> jobDefinitions = Arrays.asList(jobDefinition, jobDefinition1);

        exception.expect(InvalidJobConfiguration.class);
        exception.expectMessage("Reader SQL is empty for the job 'test1'");

        JobDefinitionValidator.validate(jobDefinitions);

    }

    @Test
    public void shouldThrowInvalidJobConfigurationExceptionIfReaderSQLIsNull() {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setReaderSql("select * from program");
        jobDefinition.setTableName("table");
        jobDefinition.setType(GENERIC);

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setReaderSql(null);
        jobDefinition1.setTableName("table1");
        jobDefinition1.setType(GENERIC);

        List<JobDefinition> jobDefinitions = Arrays.asList(jobDefinition, jobDefinition1);

        exception.expect(InvalidJobConfiguration.class);
        exception.expectMessage("Reader SQL is empty for the job 'test1");

        JobDefinitionValidator.validate(jobDefinitions);

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

        List<JobDefinition> jobDefinitions = Arrays.asList(jobDefinition, jobDefinition1);

        exception.expect(InvalidJobConfiguration.class);
        exception.expectMessage("Table name is empty for the job 'test1'");

        JobDefinitionValidator.validate(jobDefinitions);

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

        List<JobDefinition> jobDefinitions = Arrays.asList(jobDefinition, jobDefinition1);

        exception.expect(InvalidJobConfiguration.class);
        exception.expectMessage("Table name is empty for the job 'test1'");

        JobDefinitionValidator.validate(jobDefinitions);

    }

    @Test
    public void shouldGiveTrueForValidJobConfigurations() {
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
        jobDefinition2.setTableName("table2");
        jobDefinition2.setReaderSql("select * from program2");
        jobDefinition2.setType(GENERIC);

        jobDefinitions.add(jobDefinition);
        jobDefinitions.add(jobDefinition1);
        jobDefinitions.add(jobDefinition2);

        assertTrue(JobDefinitionValidator.validate(jobDefinitions));
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