package org.bahmni.mart.config.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class JobDefinitionValidator {

    private static final Logger logger = LoggerFactory.getLogger(JobDefinitionValidator.class);

    public static boolean validate(List<JobDefinition> jobDefinitions) {
        Set<String> jobNames = new HashSet<>();
        Set<String> tableNames = new HashSet<>();
        for (JobDefinition jobDefinition : jobDefinitions) {
            String jobName = jobDefinition.getName();
            String tableName = jobDefinition.getTableName();

            if (!(jobNames.add(jobName) && tableNames.add(tableName))) {
                logger.error(String.format("Invalid job configuration found for %s. " +
                        "[Either job name \"%s\" or table name \"%s\" is duplicate]", jobName, jobName, tableName));
                return false;
            }
        }
        return true;
    }
}
