package org.bahmni.mart.config.job;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.BatchUtils;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class JobDefinitionUtil {

    private static final String TO_SPLIT_FROM = "(?i)from";
    private static final String TO_SPLIT_SELECT = "(?i)select";
    public static final String OBS_JOB_TYPE = "obs";
    public static final String BACTERIOLOGY_JOB_TYPE = "bacteriology";

    public static String getReaderSQLByIgnoringColumns(List<String> columnsToIgnore, String readerSQL) {
        if (StringUtils.isEmpty(readerSQL) || CollectionUtils.isEmpty(columnsToIgnore)) {
            return readerSQL;
        }

        String[] sqlSubstrings = readerSQL.split(TO_SPLIT_FROM, 2);
        String[] readerSQLColumns = sqlSubstrings[0].replaceFirst(TO_SPLIT_SELECT, "").split(",");

        return getUpdatedSQL(getUpdatedColumns(Arrays.asList(readerSQLColumns),
                new HashSet<>(columnsToIgnore)), sqlSubstrings[1].trim());
    }

    private static String getUpdatedSQL(List<String> updatedColumns, String query) {
        return updatedColumns.isEmpty() ? "" :
                String.format("select %s from %s", StringUtils.join(updatedColumns, ", "), query);
    }

    private static String getColumnName(String sql) {
        for (String splitToken : new String[]{"\\.", " (?i)as "}) {
            String[] tokens = sql.split(splitToken);
            sql = tokens[tokens.length - 1];
        }
        return sql.replaceAll("`", "");
    }

    private static List<String> getUpdatedColumns(List<String> readerSQLColumns, HashSet<String> ignoredColumns) {
        return readerSQLColumns.stream().map(String::trim)
                .filter(column -> ignoredColumns.stream().noneMatch(getColumnName(column)::equals))
                .collect(Collectors.toList());
    }

    public static List<String> getIgnoreConceptNamesForObsJob(List<JobDefinition> jobDefinitions) {
        return getDefaultIfNotPresent(getJobDefinitionByType(jobDefinitions, OBS_JOB_TYPE).getColumnsToIgnore());
    }

    public static List<String> getIgnoreConceptNamesForJob(JobDefinition jobDefinition) {
        return getDefaultIfNotPresent(jobDefinition.getColumnsToIgnore());
    }

    public static List<String> getSeparateTableNamesForObsJob(List<JobDefinition> jobDefinitions) {
        return getDefaultIfNotPresent(getJobDefinitionByType(jobDefinitions, OBS_JOB_TYPE).getSeparateTables());
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
        Resource readerSqlResource = ReaderSQLFileLoader.loadResource(jobDefinition.getReaderSqlFilePath());
        return BatchUtils.convertResourceOutputToString(readerSqlResource);
    }

    public static List<String> getSeparateTableNamesForBacteriologyJob(List<JobDefinition> jobDefinitions) {
        JobDefinition bacteriologyJob = getJobDefinitionByType(jobDefinitions, BACTERIOLOGY_JOB_TYPE);
        return getDefaultIfNotPresent(bacteriologyJob.getSeparateTables());
    }

}
