package org.bahmni.mart.table.model;

import org.bahmni.mart.config.job.model.EavAttributes;
import org.bahmni.mart.helper.AttributeColumnName;
import org.bahmni.mart.table.domain.TableData;

public class EAV {

    private TableData tableData;
    private EavAttributes eavAttributes;
    private String typeColumnName;

    public EAV(TableData tableData, EavAttributes eavAttributes) {
        this.tableData = tableData;
        this.eavAttributes = eavAttributes;
        typeColumnName = AttributeColumnName.valueOf(eavAttributes.getAttributeTypeTableName()).getDatatype();
    }

    public TableData getTableData() {
        return tableData;
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    public String getTypeColumnName() {
        return typeColumnName;
    }

    public EavAttributes getEavAttributes() {
        return eavAttributes;
    }

    public void setEavAttributes(EavAttributes eavAttributes) {
        this.eavAttributes = eavAttributes;
    }
}
