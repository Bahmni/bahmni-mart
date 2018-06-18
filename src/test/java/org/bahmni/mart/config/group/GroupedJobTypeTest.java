package org.bahmni.mart.config.group;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class GroupedJobTypeTest {

    @Test
    public void shouldReturnTrueIfTheEnumContainsProgramsType() {
        assertTrue(GroupedJobType.contains("programs"));
    }
}