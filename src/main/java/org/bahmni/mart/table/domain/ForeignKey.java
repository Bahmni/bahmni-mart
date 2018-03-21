package org.bahmni.mart.table.domain;

import java.util.Objects;

public class ForeignKey {
    private String referenceColumn;
    private String referenceTable;

    public ForeignKey(String referenceColumn, String referenceTable) {
        this.referenceColumn = referenceColumn;
        this.referenceTable = referenceTable;
    }

    public String getReferenceColumn() {
        return referenceColumn;
    }

    public void setReferenceColumn(String referenceColumn) {
        this.referenceColumn = referenceColumn;
    }

    public String getReferenceTable() {
        return referenceTable;
    }

    public void setReferenceTable(String referenceTable) {
        this.referenceTable = referenceTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForeignKey that = (ForeignKey) o;
        return Objects.equals(referenceColumn, that.referenceColumn) &&
                Objects.equals(referenceTable, that.referenceTable);
    }

    @Override
    public int hashCode() {

        return Objects.hash(referenceColumn, referenceTable);
    }
}

