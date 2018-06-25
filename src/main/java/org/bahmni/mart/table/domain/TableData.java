package org.bahmni.mart.table.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;

public class TableData {
    private String name;
    private List<TableColumn> columns;

    public TableData(String name) {
        this.name = name;
        this.columns = new ArrayList<TableColumn>();
    }

    public TableData() {
        this(null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<TableColumn> columns) {
        this.columns = columns;
    }

    public void addColumn(TableColumn column) {
        this.columns.add(column);
    }

    public void addAllColumns(List<TableColumn> columns) {
        this.columns.addAll(columns);
    }

    @Override
    public boolean equals(Object obj) {
        if (isNull(obj) || this.getClass() != obj.getClass())
            return false;

        TableData that = (TableData) obj;
        if (this == that) {
            return true;
        }

        boolean isTableNameMatches = Objects.equals(this.getName(), that.getName());
        if (isTableNameMatches && isNull(this.getColumns()) && isNull(that.getColumns())) {
            return true;
        }

        return isTableNameMatches &&
                nonNull(this.getColumns()) && nonNull(that.getColumns()) &&
                isEqualCollection(this.getColumns(), that.getColumns());
    }
}
