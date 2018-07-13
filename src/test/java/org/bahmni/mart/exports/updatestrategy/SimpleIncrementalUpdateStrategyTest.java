package org.bahmni.mart.exports.updatestrategy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
public class SimpleIncrementalUpdateStrategyTest {
    @Test
    public void shouldGiveTrueAsMetadataChangeStatus() {
        SimpleIncrementalUpdateStrategy simpleIncrementalUpdater = new SimpleIncrementalUpdateStrategy();
        assertTrue(simpleIncrementalUpdater.getMetaDataChangeStatus("table name", "job name"));
    }
}