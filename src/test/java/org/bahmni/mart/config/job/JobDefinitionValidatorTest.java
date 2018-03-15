package org.bahmni.mart.config.job;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JobDefinitionValidatorTest {

    @Test
    public void shouldGiveTrueForValidJobConfigurations() {
        List<JobDefinition> jobDefinitions = new ArrayList<>();

        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setTableName("table");

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setTableName("table1");

        JobDefinition jobDefinition2 = new JobDefinition();
        jobDefinition2.setName("test2");
        jobDefinition2.setTableName("table2");

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

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test");
        jobDefinition1.setTableName("table1");

        JobDefinition jobDefinition2 = new JobDefinition();
        jobDefinition2.setName("test2");
        jobDefinition2.setTableName("table2");

        jobDefinitions.add(jobDefinition);
        jobDefinitions.add(jobDefinition1);
        jobDefinitions.add(jobDefinition2);
        assertFalse(JobDefinitionValidator.validate(jobDefinitions));
    }

    @Test
    public void shouldGiveFalseForDuplicateTableNameInJobConfigurations() {
        List<JobDefinition> jobDefinitions = new ArrayList<>();

        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("test");
        jobDefinition.setTableName("table");

        JobDefinition jobDefinition1 = new JobDefinition();
        jobDefinition1.setName("test1");
        jobDefinition1.setTableName("table");

        JobDefinition jobDefinition2 = new JobDefinition();
        jobDefinition2.setName("test2");
        jobDefinition2.setTableName("table2");

        jobDefinitions.add(jobDefinition);
        jobDefinitions.add(jobDefinition1);
        jobDefinitions.add(jobDefinition2);
        assertFalse(JobDefinitionValidator.validate(jobDefinitions));
    }
}