package org.bahmni.mart.config.job.model;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class IncrementalUpdateConfig {
    private String updateOn;
    private String eventCategory;
    private String openmrsTableName;

    public String getUpdateOn() {
        return updateOn;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public String getOpenmrsTableName() {
        return openmrsTableName;
    }

    public boolean isValid() {
        return isNotEmpty(updateOn) && isNotEmpty(eventCategory) && isNotEmpty(openmrsTableName);
    }
}
