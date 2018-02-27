package org.bahmni.analytics.exports;

import org.bahmni.analytics.BatchUtils;
import org.bahmni.analytics.form.domain.Obs;
import org.bahmni.analytics.table.domain.TableData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.analytics.table.FormTableMetadataGenerator.getProcessedName;


public class ObsRecordExtractorForTable {

    private String tableName = "";

    private List<Map<String, String>> recordList = new ArrayList<>();

    public ObsRecordExtractorForTable(String tableName) {
        this.tableName = tableName;
    }

    public void execute(List<? extends List<Obs>> items, TableData tableData) {
        for (List<Obs> record : items) {
            if (record.size() == 0) {
                continue;
            }

            Map<String, String> recordMap = new HashMap<>();

            tableData.getColumns().forEach(tableColumn -> recordMap.put(tableColumn.getName(), null));

            mapRecordsWithColumns(tableData, record, recordMap);

            mapPatientAndEncounterIds(tableData, record, recordMap);
            recordList.add(recordMap);
        }
    }

    private void mapRecordsWithColumns(TableData tableData, List<Obs> record, Map<String, String> recordMap) {
        record.forEach(obs -> {
            final String[] value = {""};
            tableData.getColumns().forEach(tableColumn -> {
                String tableColumnName = tableColumn.getName();
                if (tableColumnName.equals(getProcessedName(obs.getField().getName()))) {
                    value[0] = BatchUtils.getPostgresCompatibleValue(obs.getValue(), tableColumn.getType());
                    recordMap.replace(tableColumnName, value[0]);
                } else if (tableColumnName.contains("id_") && recordMap.get(tableColumnName) == null) {
                    if (tableColumn.getReference() != null && obs.getParentName() != null &&
                            tableColumnName.equals("id_" + getProcessedName(obs.getParentName()))) {
                        value[0] = BatchUtils.getPostgresCompatibleValue(obs.getParentId().toString(),
                                tableColumn.getType());
                        recordMap.replace(tableColumn.getName(), value[0]);
                    } else if (tableColumn.getReference() == null) {
                        value[0] = BatchUtils.getPostgresCompatibleValue(obs.getId().toString(),
                                tableColumn.getType());
                        recordMap.replace(tableColumn.getName(), value[0]);
                    }
                }
            });
        });
    }

    private void mapPatientAndEncounterIds(TableData tableData, List<Obs> record, Map<String, String> recordMap) {
        tableData.getColumns().forEach(tableColumn -> {
            final String[] value = {""};
            Obs obs = record.get(0);
            String tableColumnName = tableColumn.getName();
            if (tableColumnName.equals("encounter_id") && recordMap.get(tableColumnName) == null) {
                value[0] = BatchUtils.getPostgresCompatibleValue(obs.getEncounterId(), tableColumn.getType());
                recordMap.replace(tableColumnName, value[0]);
            } else if (tableColumnName.equals("patient_id") && recordMap.get(tableColumnName) == null) {
                value[0] = BatchUtils.getPostgresCompatibleValue(obs.getPatientId(), tableColumn.getType());
                recordMap.replace(tableColumnName, value[0]);
            }
        });
    }

    public List<Map<String, String>> getRecordList() {
        return recordList;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setRecordList(List<Map<String, String>> recordList) {
        this.recordList = recordList;
    }


}
