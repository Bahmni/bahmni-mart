package org.bahmni.mart.config.job;

import java.util.List;

public class SeparateTableConfig {

    private boolean enableForAddMoreAndMultiSelect;
    private List<String> separateTables;

    public boolean isEnableForAddMoreAndMultiSelect() {
        return enableForAddMoreAndMultiSelect;
    }

    public List<String> getSeparateTables() {
        return separateTables;
    }
}
