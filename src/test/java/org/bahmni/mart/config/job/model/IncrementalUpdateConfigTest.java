package org.bahmni.mart.config.job.model;

import org.junit.Before;
import org.junit.Test;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IncrementalUpdateConfigTest {

    private IncrementalUpdateConfig incrementalUpdateConfig;

    @Before
    public void setUp() throws Exception {
        incrementalUpdateConfig = new IncrementalUpdateConfig();
    }

    @Test
    public void shouldReturnTrueIfAllFieldsArePresent() throws NoSuchFieldException, IllegalAccessException {
        setValuesForMemberFields(incrementalUpdateConfig, "updateOn", "update_on");
        setValuesForMemberFields(incrementalUpdateConfig, "eventCategory", "test");
        setValuesForMemberFields(incrementalUpdateConfig, "openmrsTableName", "table_name");

        assertTrue(incrementalUpdateConfig.isValid());
    }

    @Test
    public void shouldReturnFalseIfUpdateOnIsNotPresent() throws NoSuchFieldException, IllegalAccessException {
        setValuesForMemberFields(incrementalUpdateConfig, "eventCategory", "test");
        setValuesForMemberFields(incrementalUpdateConfig, "openmrsTableName", "table_name");

        assertFalse(incrementalUpdateConfig.isValid());
    }

    @Test
    public void shouldReturnFalseIfEventCategoryIsNotPresent() throws NoSuchFieldException, IllegalAccessException {
        setValuesForMemberFields(incrementalUpdateConfig, "updateOn", "update_on");
        setValuesForMemberFields(incrementalUpdateConfig, "openmrsTableName", "table_name");

        assertFalse(incrementalUpdateConfig.isValid());
    }

    @Test
    public void shouldReturnFalseIfOpenmrsTableNameIsNotPresent() throws NoSuchFieldException, IllegalAccessException {
        setValuesForMemberFields(incrementalUpdateConfig, "updateOn", "update_on");
        setValuesForMemberFields(incrementalUpdateConfig, "eventCategory", "test");

        assertFalse(incrementalUpdateConfig.isValid());
    }
}