package org.bahmni.mart.config.job;

import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class JobDefinitionValidator {

    private static final Logger logger = LoggerFactory.getLogger(JobDefinitionValidator.class);
    private static final String GENERIC = "generic";


    public static boolean validate(List<JobDefinition> jobDefinitions) {
        List<JobDefinition> genericJobDefinitions = jobDefinitions.stream().filter(jobDefinition ->
                jobDefinition.getType().equals(GENERIC)).collect(Collectors.toList());
        return validateReaderSQLsNotEmpty(genericJobDefinitions) && validateTableNamesNotEmpty(genericJobDefinitions) &&
                validateJobNamesAndTableNamesAreUnique(genericJobDefinitions);
    }

    private static boolean validateJobNamesAndTableNamesAreUnique(List<JobDefinition> jobDefinitions) {
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

    public static boolean validateTableNamesNotEmpty(List<JobDefinition> jobDefinitions) {
        return jobDefinitions.stream().noneMatch(jobDefinition -> {
            boolean isEmptyOrNullTable = jobDefinition.getTableName() == null || jobDefinition.getTableName().isEmpty();
            if (isEmptyOrNullTable) {
                String message = String.format("Table name is empty for the job '%s'", jobDefinition.getName());
                logger.error(message);
                throw new InvalidJobConfiguration(message);
            }
            return isEmptyOrNullTable;
        }
        );
    }

    private static boolean validateReaderSQLsNotEmpty(List<JobDefinition> jobDefinitions) {
        return jobDefinitions.stream().noneMatch(jobDefinition -> {
            boolean isEmptyOrNullSQL = jobDefinition.getReaderSql() == null || jobDefinition.getReaderSql().isEmpty();
            if (isEmptyOrNullSQL) {
                String message = String.format("Reader SQL is empty for the job '%s'", jobDefinition.getName());
                logger.error(message);
                throw new InvalidJobConfiguration(message);
            }
            return isEmptyOrNullSQL;
        }
        );
    }
}
