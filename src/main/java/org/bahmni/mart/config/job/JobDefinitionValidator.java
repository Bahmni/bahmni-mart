package org.bahmni.mart.config.job;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.mart.config.job.model.CodeConfig;
import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class JobDefinitionValidator {

    private static final Logger logger = LoggerFactory.getLogger(JobDefinitionValidator.class);
    private static final String CUSTOM_SQL = "customSql";

    public static boolean validate(List<JobDefinition> jobDefinitions) {
        boolean areJobNamesUnique = verifyAllJobNamesShouldBeUnique(jobDefinitions);
        boolean hasValidIncrementalUpdateConfig = verifyIncrementalUpdateConfig(jobDefinitions);
        List<JobDefinition> genericJobDefinitions = jobDefinitions.stream().filter(jobDefinition ->
                CUSTOM_SQL.equals(jobDefinition.getType())).collect(Collectors.toList());
        return areJobNamesUnique && hasValidIncrementalUpdateConfig &&
                hasNoEmptyReaderSqlOrTableName(genericJobDefinitions) &&
                hasUniqueJobNamesAndTableNames(genericJobDefinitions);
    }

    private static boolean verifyIncrementalUpdateConfig(List<JobDefinition> jobDefinitions) {
        return jobDefinitions.stream()
                .allMatch(jobDefinition -> isValidIncrementalUpdateConfig(jobDefinition.getIncrementalUpdateConfig()));
    }

    private static boolean isValidIncrementalUpdateConfig(IncrementalUpdateConfig incrementalUpdateConfig) {
        return isNull(incrementalUpdateConfig) || isNotEmpty(incrementalUpdateConfig.getUpdateOn());
    }

    private static boolean verifyAllJobNamesShouldBeUnique(List<JobDefinition> jobDefinitions) {
        Set<String> jobNames = new HashSet<>();
        for (JobDefinition jobDefinition : jobDefinitions) {
            boolean isAdded = jobNames.add(jobDefinition.getName());
            if (!isAdded) {
                logger.error(String.format("Duplicate jobs found with the same name '%s'", jobDefinition.getName()));
                return false;
            }
        }
        return true;
    }

    private static boolean hasNoEmptyReaderSqlOrTableName(List<JobDefinition> jobDefinitions) {
        return jobDefinitions.stream().noneMatch(JobDefinitionValidator::isInvalidJobDefinition);
    }

    private static boolean hasUniqueJobNamesAndTableNames(List<JobDefinition> jobDefinitions) {
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

    private static boolean isInvalidJobDefinition(JobDefinition jobDefinition) {
        if (isEmpty(jobDefinition.getTableName())) {
            logger.error(String.format("Table name is empty for the job '%s'", jobDefinition.getName()));
            return true;
        }

        if (isEmpty(jobDefinition.getReaderSql()) && isEmpty(jobDefinition.getSourceFilePath())) {
            logger.error(String.format("Reader SQL(or Reader SQL file path) is empty for the job '%s'",
                    jobDefinition.getName()));
            return true;
        }
        return false;
    }

    public static boolean isValid(List<CodeConfig> codeConfigs) {
        if (codeConfigs == null || codeConfigs.isEmpty()) {
            return false;
        }

        boolean isValid = true;
        for (CodeConfig codeConfig : codeConfigs) {
            isValid &= codeConfig != null && !isEmpty(codeConfig.getSource()) &&
                    !CollectionUtils.isEmpty(codeConfig.getColumnsToCode());
        }
        return isValid;
    }
}
