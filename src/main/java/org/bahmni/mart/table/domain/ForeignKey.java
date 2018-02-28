package org.bahmni.mart.table.domain;

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
}

