package org.bahmni.analytics.form;

import org.bahmni.analytics.form.domain.BahmniForm;
import org.bahmni.analytics.form.domain.Concept;
import org.bahmni.analytics.form.domain.Obs;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ObsFieldExtractorTest {

    private ObsFieldExtractor fieldExtractor;

    @Before
    public void setUp() throws Exception {
        BahmniForm form = new BahmniForm();
        form.setFormName(new Concept(0, "Blood Pressure", 1));
        form.addField(new Concept(1, "Systolic", 0));
        form.addField(new Concept(2, "Diastolic", 0));
        BahmniForm parentForm = new BahmniForm();
        form.setParent(parentForm);
        fieldExtractor = new ObsFieldExtractor(form);
    }

    @Test
    public void shouldExtractObsListToObjectArray() {
        List<Obs> obsList = new ArrayList<>();
        int obsGroupIdForCsv = 1;
        int rootObsId = 0;
        String treatmentNumber = "AB1234";
        obsList.add(new Obs(treatmentNumber, obsGroupIdForCsv, rootObsId, new Concept(1, "Systolic", 0), "120"));
        obsList.add(new Obs(treatmentNumber, obsGroupIdForCsv, rootObsId, new Concept(2, "Diastolic", 0), "80"));

        List<Object> result = Arrays.asList(fieldExtractor.extract(obsList));

        assertEquals(5, result.size());
        assertEquals(obsGroupIdForCsv, result.get(0));
        assertEquals(rootObsId, result.get(1));
        assertEquals(treatmentNumber, result.get(2));
        assertEquals("120", result.get(3));
        assertEquals("80", result.get(4));
    }

    @Test
    public void ensureThatSplCharsAreHandledInCSVInTheObsValue() {
        List<Obs> obsList = new ArrayList<>();
        String treatmentNumber = "AB1234";
        int obsGroupIdForCsv = 1;
        int rootObsId = 0;
        obsList.add(new Obs(treatmentNumber, obsGroupIdForCsv, rootObsId,
                new Concept(1, "Systolic", 0), "abc\ndef\tghi,klm"));

        List<Object> result = Arrays.asList(fieldExtractor.extract(obsList));

        assertEquals(5, result.size());
        assertEquals(obsGroupIdForCsv, result.get(0));
        assertEquals(rootObsId, result.get(1));
        assertEquals(treatmentNumber, result.get(2));
        assertEquals("abc def ghi klm", result.get(3));
        assertEquals(null, result.get(4));
    }

    @Test
    public void shouldReturnEmptyListWhenObsListIsEmpty() throws Exception {
        List<Obs> obsList = new ArrayList<>();

        Object[] actualOutput = fieldExtractor.extract(obsList);

        assertNotNull(actualOutput);
        assertEquals(0, actualOutput.length);
    }
}
