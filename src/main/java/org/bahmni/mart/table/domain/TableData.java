package org.bahmni.mart.table.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class TableData {
    private String name;
    private List<TableColumn> columns;

    public TableData(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
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

    public List<TableColumn> getPrimaryKeyColumns() {
        return columns.stream().filter(TableColumn::isPrimaryKey).collect(Collectors.toList());
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

        return Objects.equals(this.getName(), that.getName()) && isColumnListMatches(that);
    }

    private boolean isColumnListMatches(TableData that) {
        if (isNull(this.getColumns()) && isNull(that.getColumns())) {
            return true;
        }
        return nonNull(this.getColumns()) && nonNull(that.getColumns()) &&
                this.getColumns().size() == that.getColumns().size() &&
                this.getColumns().containsAll(that.getColumns());
    }
}
