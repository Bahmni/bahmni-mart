package org.bahmni.mart.table;

import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpecialCharacterResolver {

    private static final String ALPHA_NUMERIC_UNDERSCORE_REGEX = "[^a-zA-Z0-9_]+";

    private static Map<String, Map<String, String>> tableToColumnsMap = new LinkedHashMap<>();
    private static Map<String, String> updatedToActualTableNames = new LinkedHashMap<>();

    public static void resolveTableData(TableData tableData) {

        Map<String, String> updatedToActualColumnNamesMap = getUpdatedToActualColumnNamesMap(tableData);

        String actualTableName = tableData.getName();
        String updatedTableName = getUpdatedStringName(updatedToActualTableNames, actualTableName);
        tableData.setName(updatedTableName);

        updatedToActualTableNames.put(updatedTableName, actualTableName);
        tableToColumnsMap.put(actualTableName, updatedToActualColumnNamesMap);
    }

    public static String getActualColumnName(TableData tableData, TableColumn tableColumn) {

        String actualTableName = updatedToActualTableNames.get(tableData.getName());
        return tableToColumnsMap.get(actualTableName).get(tableColumn.getName());
    }

    private static Map<String, String> getUpdatedToActualColumnNamesMap(TableData tableData) {

        Map<String, String> updatedToActualColumnNamesMap = new LinkedHashMap<>();

        for (TableColumn tableColumn : tableData.getColumns()) {

            String actualColumnName = tableColumn.getName();
            String updatedColumnName = getUpdatedStringName(updatedToActualColumnNamesMap, actualColumnName);

            updatedToActualColumnNamesMap.put(updatedColumnName, actualColumnName);
            tableColumn.setName(updatedColumnName);
        }
        return updatedToActualColumnNamesMap;
    }

    private static String getUpdatedStringName(Map<String, String> updatedToActualStringMap, String actualString) {

        String updatedString = replaceWithUnderscore(actualString);

        int underscoreAppender = 1;

        while (updatedToActualStringMap.containsKey(updatedString)) {
            updatedString = replaceWithUnderscore(actualString, Integer.toString(underscoreAppender++));
        }
        return updatedString;
    }

    private static String replaceWithUnderscore(String actualString) {

        return replaceWithUnderscore(actualString, "");
    }

    private static String replaceWithUnderscore(String conceptName, String identifier) {

        return conceptName.replaceAll(ALPHA_NUMERIC_UNDERSCORE_REGEX, String.format("_%s", identifier))
                .replaceAll(String.format("(_+%s)\\1+", identifier), String.format("_%s", identifier));
    }
}
