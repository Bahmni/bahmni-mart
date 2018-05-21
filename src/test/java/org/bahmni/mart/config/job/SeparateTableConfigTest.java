package org.bahmni.mart.config.job;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SeparateTableConfigTest {

    @Test
    public void shouldReturnTrueIfSeparateTableEnableFlagIsNull() {

        SeparateTableConfig separateTableConfig = new SeparateTableConfig();

        assertTrue(separateTableConfig.isEnableForAddMoreAndMultiSelect());
    }
}