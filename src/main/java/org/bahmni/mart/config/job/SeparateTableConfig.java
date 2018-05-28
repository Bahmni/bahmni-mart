package org.bahmni.mart.config.job;

import java.util.List;

public class SeparateTableConfig {

    private Boolean enableForAddMoreAndMultiSelect;

    private List<String> separateTables;

    public List<String> getSeparateTables() {
        return separateTables;
    }

    public Boolean getEnableForAddMoreAndMultiSelect() {
        return enableForAddMoreAndMultiSelect;
    }
}
