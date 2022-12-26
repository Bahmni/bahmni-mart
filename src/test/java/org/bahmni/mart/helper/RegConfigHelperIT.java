package org.bahmni.mart.helper;

import org.bahmni.mart.AbstractBaseBatchIT;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
@Ignore
public class RegConfigHelperIT extends AbstractBaseBatchIT {
    @Autowired
    private RegConfigHelper regConfigHelper;

    @Test
    public void shouldGiveAllRegConceptNames() {
        List<String> expected = Arrays.asList("Nutritional", "Fee Information", "Nutritional Temp");

        List<String> regConcepts = regConfigHelper.getRegConcepts();
        assertEquals(3, regConcepts.size());
        assertTrue(regConcepts.containsAll(expected));
    }
}