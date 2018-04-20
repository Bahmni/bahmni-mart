package org.bahmni.mart.config.job;

import java.util.List;

public class CodeConfig {

    private String source;
    private String type;
    private List<String> columnsToCode;

    public String getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public List<String> getColumnsToCode() {
        return columnsToCode;
    }
}