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
        int rootObsId = 0;
        obsList.add(new Obs(1, rootObsId, new Concept(1, "Systolic", 0), "120"));
        obsList.add(new Obs(2, rootObsId, new Concept(2, "Diastolic", 0), "80"));

        List<Object> result = Arrays.asList(fieldExtractor.extract(obsList));

        assertEquals(4, result.size());
        assertEquals(1, result.get(0));
        assertEquals(rootObsId, result.get(1));
        assertEquals("120", result.get(2));
        assertEquals("80", result.get(3));
    }

    @Test
    public void ensureThatSplCharsAreHandledInCSVInTheObsValue() {
        List<Obs> obsList = new ArrayList<>();
        int obsId = 1;
        int rootObsId = 0;
        obsList.add(new Obs(obsId, rootObsId,
                new Concept(1, "Systolic", 0), "abc\ndef\tghi,klm"));

        List<Object> result = Arrays.asList(fieldExtractor.extract(obsList));

        assertEquals(4, result.size());
        assertEquals(obsId, result.get(0));
        assertEquals(rootObsId, result.get(1));
        assertEquals("abc def ghi klm", result.get(2));
        assertEquals(null, result.get(3));
    }

    @Test
    public void shouldReturnEmptyListWhenObsListIsEmpty() throws Exception {
        List<Obs> obsList = new ArrayList<>();

        Object[] actualOutput = fieldExtractor.extract(obsList);

        assertNotNull(actualOutput);
        assertEquals(0, actualOutput.length);
    }

    @Test
    public void shouldNotAddParentConceptIfParentIsNotPresent() {
        BahmniForm form = new BahmniForm();
        form.setFormName(new Concept(0, "Blood Pressure", 1));
        form.addField(new Concept(1, "Systolic", 0));
        form.addField(new Concept(2, "Diastolic", 0));
        fieldExtractor = new ObsFieldExtractor(form);

        List<Obs> obsList = new ArrayList<>();
        int rootObsId = 0;
        obsList.add(new Obs(1, rootObsId, new Concept(1, "Systolic", 0), "120"));
        obsList.add(new Obs(2, rootObsId, new Concept(2, "Diastolic", 0), "80"));

        List<Object> result = Arrays.asList(fieldExtractor.extract(obsList));

        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals("120", result.get(1));
        assertEquals("80", result.get(2));
    }
}
