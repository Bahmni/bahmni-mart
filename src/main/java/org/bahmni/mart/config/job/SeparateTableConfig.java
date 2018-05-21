package org.bahmni.mart.config.job;

import java.util.List;

public class SeparateTableConfig {

    private Boolean enableForAddMoreAndMultiSelect;
    private List<String> separateTables;

    public boolean isEnableForAddMoreAndMultiSelect() {
        return enableForAddMoreAndMultiSelect == null ? true : enableForAddMoreAndMultiSelect;
    }

    public List<String> getSeparateTables() {
        return separateTables;
    }
}
