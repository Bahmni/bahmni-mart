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

    public String getAttributeTableName() {
        return attributeTableName;
    }

    public String getValueTableJoiningId() {
        return valueTableJoiningId;
    }

    public String getTypeTableJoiningId() {
        return typeTableJoiningId;
    }

    public String getValueColumnName() {
        return valueColumnName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

}
