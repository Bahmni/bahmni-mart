package org.bahmni.mart.config.view;

public class ViewDefinition {

    private String name;
    private String viewSQL;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getViewSQL() {
        return viewSQL;
    }

    public void setViewSQL(String viewSQL) {
        this.viewSQL = viewSQL;
    }
}
