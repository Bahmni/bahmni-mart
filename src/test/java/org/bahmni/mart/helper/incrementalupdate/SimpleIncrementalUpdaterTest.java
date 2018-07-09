package org.bahmni.mart.helper.incrementalupdate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
public class SimpleIncrementalUpdaterTest {
    @Test
    public void shouldGiveTrueAsMetadataChangeStatus() {
        SimpleIncrementalUpdater simpleIncrementalUpdater = new SimpleIncrementalUpdater();
        assertTrue(simpleIncrementalUpdater.getMetaDataChangeStatus("table name"));
    }
}