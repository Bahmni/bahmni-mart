package org.bahmni.mart.config.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobDefinitionUtil {

    public static final String TO_SPLIT_FROM = "(?i)from";
    public static final String FROM = "from";
    public static final String TO_SPLIT_SELECT = "(?i)select";
    public static final String SELECT = "select";

    public static String getReaderSQLByIgnoringColumns(JobDefinition jobDefinition) {

        List<String> columnsToIgnore = jobDefinition.getColumnsToIgnore();
        String readerSQL = jobDefinition.getReaderSql();
        if (readerSQL == null || readerSQL.isEmpty() || columnsToIgnore == null || columnsToIgnore.isEmpty()) {
            return readerSQL;
        }
        String[] sqlSubstrings = readerSQL.split(TO_SPLIT_FROM);
        String[] readerSQLColumns = sqlSubstrings[0].trim().split(TO_SPLIT_SELECT)[1].trim().split(",");

        List<String> updatedColumns = getUpdatedColumns(readerSQLColumns, columnsToIgnore);

        return getUpdatedSQL(updatedColumns, sqlSubstrings[1]);
    }

    private static String getUpdatedSQL(List<String> updatedColumns, String query) {
        String finalColumns = "";
        if (updatedColumns.isEmpty())
            return finalColumns;
        finalColumns = updatedColumns.toString();
        return SELECT + " " + finalColumns.substring(1, finalColumns.length() - 1) + " " + FROM + query;
    }

    private static String getTrimmedSql(String trimSql) {
        String finalTrimSql = trimSql.trim();
        String[] splitBy = {"\\.", " as ", " AS "};
        for (String splitToken : splitBy) {
            finalTrimSql = (finalTrimSql.split(splitToken).length > 1) ?
                    finalTrimSql.split(splitToken)[1] : finalTrimSql;
        }
        return finalTrimSql.contains("`") ? finalTrimSql.substring(1, finalTrimSql.length() - 1) : finalTrimSql;
    }

    private static List<String> getUpdatedColumns(String[] readerSQLColumns, List<String> columnsToIgnore) {
        Set<String> ignoredColumns = new HashSet<>();
        ignoredColumns.addAll(columnsToIgnore);
        List<String> updatedColumns = new ArrayList<>();

        Arrays.asList(readerSQLColumns).forEach((String readerSQLColumn) -> {
            String trimmedReaderSQLColumn = getTrimmedSql(readerSQLColumn);

            boolean isIgnored = ignoredColumns.stream().anyMatch(trimmedReaderSQLColumn::equals);
            if (!isIgnored) {
                updatedColumns.add(readerSQLColumn);
            }
        });
        return updatedColumns;
    }

}
