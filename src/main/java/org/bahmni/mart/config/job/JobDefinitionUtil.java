package org.bahmni.mart.config.job;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.model.GroupedJobConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.model.SeparateTableConfig;
import org.bahmni.mart.config.jsql.SqlParser;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

public class JobDefinitionUtil {

    public static String getReaderSQLByIgnoringColumns(List<String> columnsToIgnore, String readerSQL) {
        if (StringUtils.isEmpty(readerSQL) || CollectionUtils.isEmpty(columnsToIgnore)) {
            return readerSQL;
        }
        return SqlParser.getUpdatedReaderSql(columnsToIgnore, readerSQL);
    }

    public static List<String> getIgnoreConceptNamesForJob(JobDefinition jobDefinition) {
        return getDefaultIfNotPresent(jobDefinition.getColumnsToIgnore());
    }

    public static List<String> getSeparateTableNamesForJob(JobDefinition jobDefinition) {
        SeparateTableConfig separateTableConfig = jobDefinition.getSeparateTableConfig();
        return getDefaultIfNotPresent(separateTableConfig != null ?
                separateTableConfig.getSeparateTables() : new ArrayList<>());
    }

    private static List<String> getDefaultIfNotPresent(List<String> names) {
        return isNull(names) ? new ArrayList<>() : names;
    }

    public static JobDefinition getJobDefinitionByType(List<JobDefinition> jobDefinitions, String type) {
        return jobDefinitions.stream()
                .filter(jobDefinition -> jobDefinition.getType().equals(type))
                .findFirst().orElseGet(JobDefinition::new);
    }

    public static String getReaderSQL(JobDefinition jobDefinition) {
        if (StringUtils.isNotEmpty(jobDefinition.getReaderSql())) {
            return jobDefinition.getReaderSql();
        }
        Resource readerSqlResource = SQLFileLoader.loadResource(jobDefinition.getSourceFilePath());
        return BatchUtils.convertResourceOutputToString(readerSqlResource);
    }

    public static boolean isAddMoreMultiSelectEnabled(JobDefinition jobDefinition) {
        if (jobDefinition == null) {
            return true;
        }
        SeparateTableConfig separateTableConfig = jobDefinition.getSeparateTableConfig();
        return separateTableConfig == null || isAddMoreMultiSelectEnabled(separateTableConfig);
    }

    private static boolean isAddMoreMultiSelectEnabled(SeparateTableConfig separateTableConfig) {
        Boolean enableForAddMoreAndMultiSelect = separateTableConfig.getEnableForAddMoreAndMultiSelect();
        return enableForAddMoreAndMultiSelect == null ? true : enableForAddMoreAndMultiSelect;
    }

    public static void setCommonPropertiesToGroupedJobs(JobDefinition sourceJobDefinition,
                                                        List<JobDefinition> groupedJobDefinitions) {
        int chunkSizeToRead = sourceJobDefinition.getChunkSizeToRead();
        groupedJobDefinitions.forEach(groupedJobDefinition -> groupedJobDefinition.setChunkSizeToRead(chunkSizeToRead));
    }

    public static void setConfigToGroupedJobs(JobDefinition sourceJobDefinition,
                                              List<JobDefinition> groupedJobDefinitions) {
        List<GroupedJobConfig> groupedJobConfigs = sourceJobDefinition.getGroupedJobConfigs();
        if (groupedJobConfigs == null)
            return;
        groupedJobConfigs.forEach(groupedJobConfig -> {
            JobDefinition groupedJobDefinition = getJobDefinitionFromTable(groupedJobConfig.getTableName(),
                    groupedJobDefinitions);

            if (groupedJobDefinition != null) {
                groupedJobDefinition.setColumnsToIgnore(groupedJobConfig.getColumnsToIgnore());
                groupedJobDefinition.setCodeConfigs(groupedJobConfig.getCodeConfigs());
            }
        });
    }

    private static JobDefinition getJobDefinitionFromTable(String tableName,
                                                           List<JobDefinition> groupedJobDefinitions) {
        Optional<JobDefinition> optionalJobDefinition = groupedJobDefinitions.stream()
                .filter(jobDefinition -> {
                    String jobTableName = jobDefinition.getTableName();
                    return jobTableName != null && jobTableName.equals(tableName);
                }).findFirst();
        return optionalJobDefinition.orElse(null);
    }
}
