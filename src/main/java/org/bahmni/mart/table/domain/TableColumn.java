package org.bahmni.mart.table.domain;

import java.util.Objects;

public class TableColumn {
    private String name;
    private String type;
    private boolean isPrimaryKey;
    private ForeignKey reference;

    public TableColumn(String name, String type, boolean isPrimaryKey, ForeignKey reference) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableColumn that = (TableColumn) o;
        return isPrimaryKey == that.isPrimaryKey &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(reference, that.reference);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, type, isPrimaryKey, reference);
    }
}
