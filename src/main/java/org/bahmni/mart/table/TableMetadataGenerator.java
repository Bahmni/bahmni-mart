package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.helper.Constants;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class TableMetadataGenerator implements TableMetadataGeneratorContract {

    protected Map<String, TableData> tableDataMap = new LinkedHashMap<>();

    public List<TableData> getTableDataList() {
        return new ArrayList<>(tableDataMap.values());
    }

    public void setTableDataMap(Map<String, TableData> tableDataMap) {
        this.tableDataMap = tableDataMap;
    }

    public TableData getTableData(BahmniForm form) {
        String processedFormName = getProcessedName(form.getFormName().getName());
        return getTableDataByName(processedFormName);
    }

    public TableData getTableDataByName(String processedFormName) {
        return tableDataMap.get(processedFormName);
    }


    public static String getProcessedName(String formName) {
        return formName.trim().replaceAll("\\s+", "_").toLowerCase();
    }

    public void addMetadataForForm(BahmniForm form) {
        TableData tableData = getTableData(form);
        String formName = getProcessedName(form.getFormName().getName());
        if (tableData != null) {
            tableDataMap.remove(formName);
            List<TableColumn> foreignKeyColumn = getForeignKeyColumn(form);
            if (foreignKeyColumn != null && !tableData.getColumns().contains(foreignKeyColumn))
                tableData.addAllColumns(foreignKeyColumn);
        } else {
            tableData = new TableData(formName);
            tableData.addAllColumns(getColumns(form));
        }
        tableDataMap.put(formName, tableData);
    }

    public int getTableDataMapSize() {
        return tableDataMap.size();
    }

    public boolean hasMetadataFor(BahmniForm form) {
        return tableDataMap.containsKey(getProcessedName(form.getFormName().getName()));
    }

    protected List<TableColumn> getNonKeyColumns(BahmniForm form) {
        List<Concept> fields = form.getFields();
        List<TableColumn> columns = new ArrayList<>();
        fields.forEach(field -> {
            final String dataType = field.getDataType();
            if (dataType != null)
                columns.add(new TableColumn(getProcessedName(field.getName()),
                        Constants.getPostgresDataTypeFor(dataType),
                        false,
                        null));
        });
        return columns;
    }

    abstract protected List<TableColumn> getForeignKeyColumn(BahmniForm form);

    abstract protected List<TableColumn> getColumns(BahmniForm form);
}
