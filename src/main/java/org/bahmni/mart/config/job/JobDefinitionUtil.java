package org.bahmni.mart.config.job;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class JobDefinitionUtil {

    private static final String TO_SPLIT_FROM = "(?i)from";
    private static final String TO_SPLIT_SELECT = "(?i)select";
    private static final String OBS_JOB_TYPE = "obs";

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
        return getDefaultIfNotPresent(getObsJobDefinition(jobDefinitions).getColumnsToIgnore());
    }

    public static List<String> getSeparateTableNamesForObsJob(List<JobDefinition> jobDefinitions) {
        return getDefaultIfNotPresent(getObsJobDefinition(jobDefinitions).getSeparateTables());
    }

    private static List<String> getDefaultIfNotPresent(List<String> names) {
        return isNull(names) ? new ArrayList<>() : names;
    }

    private static JobDefinition getObsJobDefinition(List<JobDefinition> jobDefinitions) {
        return jobDefinitions.stream()
                .filter(jobDefinition -> jobDefinition.getType().equals(OBS_JOB_TYPE))
                .findFirst().orElseGet(JobDefinition::new);
    }
}
