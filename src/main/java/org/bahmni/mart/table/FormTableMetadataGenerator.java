package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.helper.Constants;
import org.bahmni.mart.table.domain.ForeignKey;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Primary
@Component("FormTableMetadataGenerator")
public class FormTableMetadataGenerator implements TableMetadataGenerator {

    private Map<String, TableData> tableDataMap = new LinkedHashMap<>();


    public List<TableData> getTableDataList() {
        return new ArrayList<>(tableDataMap.values());
    }

    public void setTableDataMap(Map<String, TableData> tableDataMap) {
        this.tableDataMap = tableDataMap;
    }

    public void addMetadataForForm(BahmniForm form) {
        TableData tableData = getTableData(form);
        String formName = getProcessedName(form.getFormName().getName());
        if (tableData != null) {
            tableDataMap.remove(formName);
            TableColumn foreignKeyColumn = getForeignKeyColumn(form);
            if (!tableData.getColumns().contains(foreignKeyColumn))
                tableData.addColumn(foreignKeyColumn);
            tableDataMap.put(formName, tableData);
        } else {
            tableData = new TableData(formName);
            tableData.addAllColumns(getColumns(form));
            tableDataMap.put(formName, tableData);
        }
    }

    private List<TableColumn> getColumns(BahmniForm form) {
        List<TableColumn> columns = new ArrayList<>();
        columns.add(getPrimaryColumn(form));
        columns.add(new TableColumn("patient_id", "integer", false, null));
        columns.add(new TableColumn("encounter_id", "integer", false, null));
        TableColumn foreignKeyColumn = getForeignKeyColumn(form);
        if (foreignKeyColumn != null)
            columns.add(foreignKeyColumn);
        columns.addAll(getNonKeyColumns(form));
        return columns;
    }

    private TableColumn getPrimaryColumn(BahmniForm form) {
        return new TableColumn(String.format("id_%s", getProcessedName(form.getFormName().getName())),
                Constants.getPostgresDataTypeFor(form.getFormName().getDataType()), true, null);
    }

    private TableColumn getForeignKeyColumn(BahmniForm form) {
        if (form.getParent() != null && form.getParent().getParent() != null) {

            Concept formParentConcept = form.getParent().getFormName();
            String formParentConceptName = formParentConcept.getName();
            String referenceTableName = getProcessedName(formParentConceptName);
            String referenceColumn = "id_" + referenceTableName;
            ForeignKey reference = new ForeignKey(referenceColumn, referenceTableName);

            return new TableColumn(referenceColumn,
                    Constants.getPostgresDataTypeFor(formParentConcept.getDataType()),
                    false,
                    reference);
        }
        return null;
    }

    private List<TableColumn> getNonKeyColumns(BahmniForm form) {
        List<Concept> fields = form.getFields();
        List<TableColumn> columns = new ArrayList<>();
        fields.forEach(field -> columns.add(new TableColumn(getProcessedName(field.getName()),
                Constants.getPostgresDataTypeFor(field.getDataType()),
                false,
                null)));
        return columns;
    }

    public TableData getTableData(BahmniForm form) {
        return tableDataMap.get(getProcessedName(form.getFormName().getName()));
    }

    public int getTableDataMapSize() {
        return tableDataMap.size();
    }

    public boolean hasMetadataFor(BahmniForm form) {
        return tableDataMap.containsKey(getProcessedName(form.getFormName().getName()));
    }

    public static String getProcessedName(String formName) {
        return formName.trim().replaceAll("\\s+", "_").toLowerCase();
    }
}
