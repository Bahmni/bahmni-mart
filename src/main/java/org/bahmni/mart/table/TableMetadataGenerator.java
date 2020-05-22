package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form2.service.FormService;
import org.bahmni.mart.helper.Constants;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

public abstract class TableMetadataGenerator implements TableMetadataGeneratorContract {

    @Autowired
    private FormService formService;

    protected Map<String, TableData> tableDataMap = new LinkedHashMap<>();

    public List<TableData> getTableDataList() {
        return new ArrayList<>(tableDataMap.values());
    }

    public void setTableDataMap(Map<String, TableData> tableDataMap) {
        this.tableDataMap = tableDataMap;
    }

    public TableData getTableData(BahmniForm form) {
        String processedFormName = getProcessedName(form.getTranslatedFormName() != null ? form.getTranslatedFormName() : form.getFormName().getName());
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
        String formName = getProcessedName(form.getTranslatedFormName() != null ? form.getTranslatedFormName() : form.getFormName().getName());
        if (tableData != null) {
            tableDataMap.remove(formName);
            List<TableColumn> foreignKeyColumns = getForeignKeyColumns(form);
            if (isNotEmpty(foreignKeyColumns) && !tableData.getColumns().containsAll(foreignKeyColumns))
                tableData.addAllColumns(foreignKeyColumns);
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

    protected abstract List<TableColumn> getForeignKeyColumns(BahmniForm form);

    protected abstract List<TableColumn> getColumns(BahmniForm form);
}
