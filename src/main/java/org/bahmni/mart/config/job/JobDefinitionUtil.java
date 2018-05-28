package org.bahmni.mart.config.job;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.jsql.SqlParser;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

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
        return getDefaultIfNotPresent(jobDefinition.getSeparateTables());
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
}
