package org.bahmni.mart.config.job;

import java.util.List;

public class GroupedJobConfig {

    private String tableName;
    private List<String> columnsToIgnore;
    private List<CodeConfig> codeConfigs;

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumnsToIgnore() {
        return columnsToIgnore;
    }

    public List<CodeConfig> getCodeConfigs() {
        return codeConfigs;
    }

}
