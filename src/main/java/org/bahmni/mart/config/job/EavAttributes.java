package org.bahmni.mart.config.job;

public class EavAttributes {
    private String attributeTypeTableName;
    private String attributeTableName;
    private String valueTableJoiningId;
    private String typeTableJoiningId;
    private String valueColumnName;
    private String primaryKey;

    public String getAttributeTypeTableName() {
        return attributeTypeTableName;
    }

    public void setAttributeTypeTableName(String attributeTypeTableName) {
        this.attributeTypeTableName = attributeTypeTableName;
    }

    public String getAttributeTableName() {
        return attributeTableName;
    }

    public void setAttributeTableName(String attributeTableName) {
        this.attributeTableName = attributeTableName;
    }

    public String getValueTableJoiningId() {
        return valueTableJoiningId;
    }

    public void setValueTableJoiningId(String valueTableJoiningId) {
        this.valueTableJoiningId = valueTableJoiningId;
    }

    public String getTypeTableJoiningId() {
        return typeTableJoiningId;
    }

    public void setTypeTableJoiningId(String typeTableJoiningId) {
        this.typeTableJoiningId = typeTableJoiningId;
    }

    public String getValueColumnName() {
        return valueColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
}
