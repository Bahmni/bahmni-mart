package org.bahmni.mart.exports.updatestrategy;

import org.springframework.stereotype.Component;

@Component
public class SimpleIncrementalUpdateStrategy extends AbstractIncrementalUpdateStrategy {
    @Override
    public boolean getMetaDataChangeStatus(String actualTableName) {
        return true;
    }
}
