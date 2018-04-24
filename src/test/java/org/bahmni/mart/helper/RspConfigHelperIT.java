package org.bahmni.mart.helper;

import org.bahmni.mart.AbstractBaseBatchIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RspConfigHelperIT extends AbstractBaseBatchIT {
    @Autowired
    private RspConfigHelper rspConfigHelper;

    @Test
    public void shouldGiveAllRspConceptNames() {
        List<String> expected = Arrays.asList("Nutritional", "Fee Information", "Nutritional Temp");

        List<String> rspConcepts = rspConfigHelper.getRspConcepts();
        assertEquals(3, rspConcepts.size());
        assertTrue(rspConcepts.containsAll(expected));
    }
}