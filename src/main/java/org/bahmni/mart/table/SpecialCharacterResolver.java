package org.bahmni.mart.table;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.bahmni.mart.table.domain.ForeignKey;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public class SpecialCharacterResolver {

    private static final String ALPHA_NUMERIC_UNDERSCORE_REGEX = "[^\\p{L}0-9_]+";
    private static final int MAX_COLUMN_NAME_LENGTH = 59;

    private static Map<String, DualHashBidiMap> tableToColumnsMap = new HashMap<>();
    private static DualHashBidiMap updatedToActualTableNames = new DualHashBidiMap();

    public static void resolveTableData(TableData tableData) {

        if (updatedToActualTableNames.containsKey(tableData.getName())) {
            return;
        }
        DualHashBidiMap updatedToActualColumnNamesMap = getUpdatedToActualColumnNamesMap(tableData);

        String actualTableName = tableData.getName();
        String updatedTableName = getUpdatedStringName(updatedToActualTableNames, actualTableName);
        tableData.setName(updatedTableName);
        updatedToActualTableNames.put(updatedTableName, actualTableName);
        tableToColumnsMap.put(actualTableName, updatedToActualColumnNamesMap);

    }

    public static String getActualColumnName(TableData tableData, TableColumn tableColumn) {

        String actualTableName = (String) updatedToActualTableNames.get(tableData.getName());
        DualHashBidiMap updatedToActualColumns = tableToColumnsMap.get(actualTableName);
        String actualColumnName = tableColumn.getName();
        return updatedToActualColumns != null ?
                (String) updatedToActualColumns.get(actualColumnName) : actualColumnName;
    }

    private static DualHashBidiMap getUpdatedToActualColumnNamesMap(TableData tableData) {

        DualHashBidiMap updatedToActualColumnNamesMap = new DualHashBidiMap();

        for (TableColumn tableColumn : tableData.getColumns()) {

            String actualColumnName = tableColumn.getName();
            String updatedColumnName = getUpdatedTableColumnName(updatedToActualColumnNamesMap, actualColumnName);

            updatedToActualColumnNamesMap.put(updatedColumnName, actualColumnName);
            tableColumn.setName(updatedColumnName);

            updateForeignKeyReferenceNames(tableColumn);

        }
        return updatedToActualColumnNamesMap;
    }

    private static String getUpdatedTableColumnName(DualHashBidiMap updatedToActualColumnNamesMap,
                                                    String actualColumnName) {
        String updatedColumnName = getUpdatedStringName(updatedToActualColumnNamesMap, actualColumnName);
        if (updatedColumnName.length() > MAX_COLUMN_NAME_LENGTH) {
            updatedColumnName = updatedColumnName.substring(0, MAX_COLUMN_NAME_LENGTH);
        }
        if (updatedToActualColumnNamesMap.containsKey(updatedColumnName)) {
            updatedColumnName = resolveSameName(updatedColumnName, updatedToActualColumnNamesMap);
        }
        return updatedColumnName;
    }

    private static String resolveSameName(String updatedColumnName, DualHashBidiMap updatedToActualColumnNamesMap) {
        long countOfNameExists = updatedToActualColumnNamesMap.keySet()
                .stream()
                .filter(columnKey -> isColumnNameExist((String) columnKey, updatedColumnName)).count();
        return updatedColumnName.concat(String.format("_%d", countOfNameExists));
    }

    private static boolean isColumnNameExist(String columnKey, String updatedColumnName) {
        if (!columnKey.startsWith(updatedColumnName)) {
            return false;
        }
        String sameName = getSameName(columnKey, updatedColumnName.length());
        return updatedColumnName.equals(sameName);
    }

    private static String getSameName(String columnKey, Integer fromIndex) {
        int underscoreIndex = columnKey.indexOf("_", fromIndex);
        return underscoreIndex == -1 ? columnKey : columnKey.substring(0, underscoreIndex);
    }

    private static void updateForeignKeyReferenceNames(TableColumn tableColumn) {
        ForeignKey foreignKeyReference = tableColumn.getReference();
        if (foreignKeyReference != null) {
            String referenceTable = foreignKeyReference.getReferenceTable();
            String referenceColumn = foreignKeyReference.getReferenceColumn();
            if (updatedToActualTableNames.containsValue(referenceTable)) {
                String str = (String) updatedToActualTableNames.getKey(referenceTable);
                foreignKeyReference.setReferenceTable(str);
            }
            if (tableToColumnsMap.containsKey(referenceTable)) {
                foreignKeyReference.setReferenceColumn((String) tableToColumnsMap.get(referenceTable)
                        .getKey(referenceColumn));
            }
        }
    }

    private static String getUpdatedStringName(Map<String, String> updatedToActualStringMap, String actualString) {

        String updatedString = replaceWithUnderscore(actualString);

        int underscoreAppender = 1;

        while (updatedToActualStringMap.containsKey(updatedString)) {
            updatedString = replaceWithUnderscore(actualString, Integer.toString(underscoreAppender++));
        }
        return updatedString;
    }

    private static String removeEndingUnderscore(String updatedString) {
        return updatedString.endsWith("_") ? updatedString.substring(0, updatedString.length() - 1) : updatedString;
    }

    private static String replaceWithUnderscore(String actualString) {

        return replaceWithUnderscore(actualString, "");
    }

    private static String replaceWithUnderscore(String conceptName, String identifier) {

        String updatedConceptName = conceptName
                .replaceAll(ALPHA_NUMERIC_UNDERSCORE_REGEX, String.format("_%s", identifier))
                .replaceAll(String.format("(_+%s)\\1+", identifier), String.format("_%s", identifier));

        return removeEndingUnderscore(updatedConceptName);
    }

    public static String getUpdatedTableNameIfExist(String actualTableName) {
        String updatedTableName = (String) updatedToActualTableNames.getKey(actualTableName);
        return updatedTableName != null ? updatedTableName : actualTableName;
    }

    public static String getUpdatedColumnName(TableData updatedTableData, String actualColumnName) {
        String actualTableName = (String) updatedToActualTableNames.get(updatedTableData.getName());
        DualHashBidiMap updatedToActualColumnsMap = tableToColumnsMap.get(actualTableName);
        return updatedToActualColumnsMap == null ? actualColumnName
                : (String) updatedToActualColumnsMap.getKey(actualColumnName);
    }

    public static String getActualTableName(String updatedTableName) {
        Object actualTableName = updatedToActualTableNames.get(updatedTableName);
        return isNull(actualTableName) ? updatedTableName : String.valueOf(actualTableName);
    }
}