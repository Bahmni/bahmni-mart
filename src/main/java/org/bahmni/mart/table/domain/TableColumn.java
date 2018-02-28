package org.bahmni.mart.table.domain;

public class TableColumn {
    private String name;
    private String type;
    private Boolean isPrimaryKey;
    private ForeignKey reference;

    public TableColumn(String name, String type, Boolean isPrimaryKey, ForeignKey reference) {
        this.name = name;
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public ForeignKey getReference() {
        return reference;
    }

    public void setReference(ForeignKey reference) {
        // Type of the baseColumn and referenceColumn should be same
        this.reference = reference;
    }
}
