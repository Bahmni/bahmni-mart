package org.bahmni.mart.exports;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.table.FormTableMetadataGenerator.addPrefixToName;
import static org.bahmni.mart.table.FormTableMetadataGenerator.getProcessedName;
import static org.bahmni.mart.table.SpecialCharacterResolver.getActualTableName;


public class ObsRecordExtractorForTable {

    private String tableName;

    private boolean isAddMoreMultiSelectEnabledForSeparateTable = true;

    private List<Map<String, String>> recordList = new ArrayList<>();

    private static final int MAX_COLUMN_NAME_LENGTH = 59;

    public ObsRecordExtractorForTable(String tableName) {
        this.tableName = tableName;
    }

    public void execute(List<? extends List<Obs>> items, TableData tableData) {
        for (List<Obs> record : items) {
            if (!record.isEmpty()) {
                Map<String, String> recordMap = new HashMap<>();
                tableData.getColumns().forEach(tableColumn -> recordMap.put(tableColumn.getName(), null));

                mapRecordsWithColumns(tableData, record, recordMap);
                mapAdditionalDetails(tableData, record, recordMap);

                recordList.add(recordMap);
            }
        }
    }

    private void mapRecordsWithColumns(TableData tableData, List<Obs> record, Map<String, String> recordMap) {
        record.forEach(obs -> tableData.getColumns().forEach(tableColumn -> {
            String tableColumnName = tableColumn.getName();
            String actualColumnName = SpecialCharacterResolver.getActualColumnName(tableData, tableColumn);
            if (getProcessedName(obs.getField().getName()).equals(actualColumnName) &&
                    isNull(recordMap, tableColumnName)) {
                replace(recordMap, tableColumnName, obs.getValue(), tableColumn.getType());
            } else if (tableColumnName.contains("id_") && isNull(recordMap, tableColumnName)) {
                if (isAddMoreMultiSelectEnabledForSeparateTable) {
                    mapConstraints(recordMap, obs, tableColumn, actualColumnName);
                } else {
                    mapObsIdAndParentObsIds(recordMap, obs, tableColumn, actualColumnName);
                }
            }
        }));
    }

    private void mapObsIdAndParentObsIds(Map<String, String> recordMap, Obs obs, TableColumn tableColumn,
                                         String actualColumnName) {
        if (obs.getParentName() != null && isConstraintName(obs.getParentName(), actualColumnName)) {
            replace(recordMap, tableColumn.getName(), obs.getParentId().toString(), tableColumn.getType());
        } else if (isConstraintName(tableName, tableColumn.getName())) {
            replace(recordMap, tableColumn.getName(), obs.getId().toString(), tableColumn.getType());
        }
    }

    private boolean isNull(Map<String, String> recordMap, String tableColumnName) {
        return recordMap.get(tableColumnName) == null;
    }

    private void mapConstraints(Map<String, String> recordMap, Obs obs,
                                TableColumn tableColumn, String actualColumnName) {
        if (isForeignKey(obs, tableColumn, actualColumnName)) {
            replace(recordMap, tableColumn.getName(), obs.getParentId().toString(), tableColumn.getType());
        } else if (tableColumn.getReference() == null && isConstraintName(tableName, tableColumn.getName())) {
            replace(recordMap, tableColumn.getName(), obs.getId().toString(), tableColumn.getType());
        }
    }

    private boolean isForeignKey(Obs obs, TableColumn tableColumn, String actualColumnName) {
        return tableColumn.getReference() != null && obs.getParentName() != null &&
                isConstraintName(obs.getParentName(), actualColumnName);
    }

    private void replace(Map<String, String> recordMap, String key, String value, String type) {
        recordMap.replace(key, BatchUtils.getPostgresCompatibleValue(value, type));
    }

    private boolean isConstraintName(String conceptName, String tableColumnName) {
        String processedColumnName = "id_" + getProcessedName(conceptName);
        if (processedColumnName.length() > MAX_COLUMN_NAME_LENGTH) {
            processedColumnName = processedColumnName.substring(0, MAX_COLUMN_NAME_LENGTH);
        }
        return tableColumnName.equals(processedColumnName);
    }

    private void mapAdditionalDetails(TableData tableData, List<Obs> record, Map<String, String> recordMap) {
        for (TableColumn tableColumn : tableData.getColumns()) {
            String tableColumnName = tableColumn.getName();
            if (isNull(recordMap, tableColumnName)) {
                replace(recordMap, tableColumnName, getValue(tableColumnName, record), tableColumn.getType());
            }
        }
    }

    private String getDateModified(List<Obs> record) {
        String dateModified = null;
        String dateCreated = getDateCreated(record);
        if (StringUtils.isEmpty(dateCreated)) {
            return null;
        }
        for (Obs obs: record) {
            if (obs.getDateCreated().compareTo(dateCreated) >= 1) {
                dateModified = obs.getDateCreated();
            }
        }
        return dateModified;
    }

    private String getDateCreated(List<Obs> record) {
        Obs formObs = getFormObs(record);
        return formObs == null ? record.get(0).getDateCreated() : formObs.getDateCreated();
    }

    private String getObsDateTime(List<Obs> record) {
        Obs formObs = getFormObs(record);
        return formObs == null ? record.get(0).getObsDateTime() : formObs.getObsDateTime();
    }

    private Obs getFormObs(List<Obs> record) {
        for (Obs obs: record) {
            String fieldName = obs.getField().getName();
            String processedFieldName = getProcessedName(fieldName);
            String processedRegPrefixFieldName = getProcessedName(addPrefixToName(fieldName, "reg"));
            String actualTableName = getActualTableName(tableName);
            if (actualTableName.equals(processedFieldName) || actualTableName.equals(processedRegPrefixFieldName)) {
                return obs;
            }
        }
        return null;
    }

    private String getValue(String columnName, List<Obs> record) {
        Obs obs = record.get(0);
        switch (columnName) {
          case "encounter_id":
              return obs.getEncounterId();
          case "patient_id":
              return obs.getPatientId();
          case "obs_datetime":
              return getObsDateTime(record);
          case "date_created":
              return getDateCreated(record);
          case "date_modified":
              return getDateModified(record);
          case "location_id":
              return obs.getLocationId();
          case "location_name":
              return obs.getLocationName();
          case "program_id":
              return obs.getProgramId();
          case "program_name":
              return obs.getProgramName();
          case "form_field_path":
              return obs.getFormFieldPath();
          case "reference_form_field_path":
              return obs.getReferenceFormFieldPath();
          default:
              return null;
        }
    }

    public List<Map<String, String>> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<Map<String, String>> recordList) {
        this.recordList = recordList;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setAddMoreMultiSelectEnabledForSeparateTables(boolean addMoreMultiSelectEnabled) {
        isAddMoreMultiSelectEnabledForSeparateTable = addMoreMultiSelectEnabled;
    }
}
