package org.bahmni.mart.config.view;

public class ViewDefinition {

    private String name;
    private String sql;
    private String sourceFilePath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }
}
