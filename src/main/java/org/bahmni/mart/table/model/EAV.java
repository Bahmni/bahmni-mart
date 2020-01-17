package org.bahmni.mart.table.model;

import org.bahmni.mart.config.job.model.EavAttributes;
import org.bahmni.mart.helper.AttributeData;
import org.bahmni.mart.table.domain.TableData;

public class EAV {

    private TableData tableData;
    private EavAttributes eavAttributes;
    private String typeColumnName;
    private String dataTypeValue;

    public EAV(TableData tableData, EavAttributes eavAttributes) {
        this.tableData = tableData;
        this.eavAttributes = eavAttributes;
        typeColumnName = AttributeData.valueOf(eavAttributes.getAttributeTypeTableName()).getColumnName();
        dataTypeValue = AttributeData.valueOf(eavAttributes.getAttributeTypeTableName()).getDataType();
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

    public String getDataTypeValue() {
        return dataTypeValue;
    }

    public EavAttributes getEavAttributes() {
        return eavAttributes;
    }

    public void setEavAttributes(EavAttributes eavAttributes) {
        this.eavAttributes = eavAttributes;
    }
}
