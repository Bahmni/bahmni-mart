package org.bahmni.mart.helper.incrementalupdate;

import org.springframework.stereotype.Component;

@Component
public class IncrementalUpdater extends AbstractIncrementalUpdater {
    @Override
    protected boolean getMetaDataChangeStatus(String actualTableName) {
        return true;
    }
}
