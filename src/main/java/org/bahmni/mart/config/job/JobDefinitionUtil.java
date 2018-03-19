package org.bahmni.mart.config.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobDefinitionUtil {

    public static final String FROM = "from";
    public static final String SELECT = "select";

    public static String getReaderSQLByIgnoringColumns(JobDefinition jobDefinition) {

        List<JobDefinition.ColumnsToIgnore> columnsToIgnore = jobDefinition.getColumnsToIgnore();
        String readerSQL = jobDefinition.getReaderSql().toLowerCase();
        if (columnsToIgnore == null || columnsToIgnore.isEmpty()) {
            return readerSQL;
        }
        String[] sqlSubstrings = readerSQL.split(FROM);
        String[] readerSQLColumns = sqlSubstrings[0].trim().split(SELECT)[1].trim().split(",");

        List<String> updatedColumns = getUpdatedColumns(readerSQLColumns, columnsToIgnore);

        return getUpdatedSQL(updatedColumns, sqlSubstrings[1]);
    }

    private static String getUpdatedSQL(List<String> updatedColumns, String query) {
        String finalColumns = updatedColumns.toString();
        return SELECT + " " + finalColumns.substring(1, finalColumns.length() - 1) + " " + FROM + query;
    }

    private static List<String> getUpdatedColumns(String[] readerSQLColumns,
                                                  List<JobDefinition.ColumnsToIgnore> columnsToIgnore) {
        Set<String> ignoredColumns = getIgnoredColumns(columnsToIgnore);
        List<String> updatedColumns = new ArrayList<>();

        Arrays.asList(readerSQLColumns).forEach((String readerSQLColumn) -> {
            String trimmedReaderSQLColumn = readerSQLColumn.trim();
            boolean isIgnored = ignoredColumns.stream().anyMatch(ignoredColumn ->
                    trimmedReaderSQLColumn.equals(ignoredColumn) || (trimmedReaderSQLColumn.contains(ignoredColumn) && readerSQLColumn.contains(" as ")));
            if (!isIgnored) {
                updatedColumns.add(trimmedReaderSQLColumn);
            }
        });
        return updatedColumns;
    }

    private static Set<String> getIgnoredColumns(List<JobDefinition.ColumnsToIgnore> columnsToIgnoreList) {
        Set<String> ignoredColumnSet = new HashSet<>();
        for (JobDefinition.ColumnsToIgnore columnsToIgnore : columnsToIgnoreList) {
            List<String> ignoredColumns = columnsToIgnore.getColumns();
            ignoredColumnSet.addAll(ignoredColumns);
        }
        return ignoredColumnSet;
    }
}
