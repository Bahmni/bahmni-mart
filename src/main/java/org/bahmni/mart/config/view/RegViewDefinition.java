package org.bahmni.mart.config.view;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.helper.RegConfigHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.bahmni.mart.table.FormTableMetadataGenerator.addPrefixToName;
import static org.bahmni.mart.table.FormTableMetadataGenerator.getProcessedName;

@Component
public class RegViewDefinition {
    //TODO: Refactor This Class Suman

    private static final String REG = "Reg";
    public static final String OBS_DATETIME = "obs_datetime";
    public static final String DATE_CREATED = "date_created";
    public static final String DATE_MODIFIED = "date_modified";

    @Autowired
    private RegConfigHelper regConfigHelper;

    @Qualifier("martNamedJdbcTemplate")
    @Autowired
    protected NamedParameterJdbcTemplate martNamedJdbcTemplate;

    public ViewDefinition getDefinition() {
        ViewDefinition viewDefinition = new ViewDefinition();
        viewDefinition.setName("registration_second_page_view");
        viewDefinition.setSql(getSql());

        return viewDefinition;
    }

    private String getSql() {
        List<String> regConcepts = regConfigHelper.getRegConcepts();
        List<String> tableNames = regConcepts.stream()
                .map(conceptName -> getProcessedName(addPrefixToName(conceptName, REG)))
                .collect(Collectors.toList());

        return tableNames.size() > 0 ? createSql(tableNames) : "";
    }

    private String createSql(List<String> tableNames) {
        List<String> excludedColumns = Arrays.asList("patient_id", "encounter_id", "visit_id", "location_id",
                "location_name", "obs_datetime", "date_created", "date_modified", "program_id", "program_name",
                "patient_program_id");

        List<Map<String, Object>> tablesMetaData = getTablesMetaData(tableNames);
        long actualTablesCount = tablesMetaData.stream().map(tableData -> tableData.get("table_name").toString())
                .distinct().count();
        if (actualTablesCount == tableNames.size()) {
            String sql = format("SELECT %s %s FROM %s", createCoalesceQueries(excludedColumns, tableNames),
                    getSelectClause(tablesMetaData, excludedColumns), tableNames.get(0));

            for (int index = 1; index < tableNames.size(); index++) {
                sql = sql.concat(format(" FULL OUTER JOIN %s ON %s", tableNames.get(index),
                        getJoining(tableNames.get(index - 1), tableNames.get(index))));
            }
            return sql;
        }
        return "";

    }

    private String createCoalesceQueries(List<String> columnNames, List<String> tableNames) {
        return columnNames.stream()
                .map(columnName -> String.format("%s, ", getCoalesceQuery(tableNames, columnName)))
                .collect(Collectors.joining());
    }

    private String getCoalesceQuery(List<String> tableNames, String columnName) {
        List<String> columnNames = tableNames.stream()
                .map(tableName -> String.format("%s.%s", tableName, columnName)).collect(Collectors.toList());

        if (OBS_DATETIME.equals(columnName) || DATE_CREATED.equals(columnName)) {
            return String.format("LEAST(%s) AS %s", StringUtils.join(columnNames, ","), columnName);
        } else if (DATE_MODIFIED.equals(columnName)) {
            return String.format("GREATEST(%s) AS %s", StringUtils.join(columnNames, ","), columnName);
        }
        return String.format("COALESCE(%s) AS %s", StringUtils.join(columnNames, ","), columnName);
    }

    private String getSelectClause(List<Map<String, Object>> tablesData, List<String> excludedColumns) {
        List<String> columns = tablesData.stream()
                .filter(tableData -> {
                    String columnName = tableData.get("column_name").toString();
                    String tableName = tableData.get("table_name").toString();
                    return !excludedColumns.contains(columnName) &&
                            !String.format("id_%s", tableName).equals(columnName);
                }).map(tableData ->
                        format("%s.%s AS %s_%s", tableData.get("table_name"), tableData.get("column_name"),
                                tableData.get("table_name"), tableData.get("column_name"))
                ).collect(Collectors.toList());

        return StringUtils.join(columns, ",");
    }

    private List<Map<String, Object>> getTablesMetaData(List<String> tableNames) {
        String sql = "SELECT column_name, table_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME in (:tableNames)" +
                " AND TABLE_SCHEMA='public';";
        Map<String, List<String>> params = new HashMap<>();
        params.put("tableNames", tableNames);

        return martNamedJdbcTemplate.queryForList(sql, params);
    }

    private String getJoining(String firstTable, String secondTable) {
        return format("%s.encounter_id = %s.encounter_id",
                secondTable, firstTable);
    }
}