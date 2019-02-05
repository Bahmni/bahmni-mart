package org.bahmni.mart.form2;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Form2ObservationProcessorTest {

    private Form2ObservationProcessor form2ObservationProcessor;

    @Before
    public void setUp() throws Exception {
        form2ObservationProcessor = new Form2ObservationProcessor();
    }

    @Test
    public void shouldProcessAndConvertTheGivenDataToObs() {
        BahmniForm form = mock(BahmniForm.class);
        when(form.getDepthToParent()).thenReturn(0);
        form2ObservationProcessor.setForm(form);
        Map<String, Object> obsRow = getObsRow();

        List<Obs> obsList = form2ObservationProcessor.process(obsRow);

        assertEquals(1, obsList.size());
        Obs obs = obsList.get(0);
        assertEquals("abc123", obs.getEncounterId());
        assertEquals("def789", obs.getPatientId());
        assertEquals(Integer.valueOf(97), obs.getId());
        Concept field = obs.getField();
        assertEquals(Integer.valueOf(7), field.getId());
        assertEquals("age", field.getName());
        assertEquals("21", obs.getValue());
        assertEquals("2015-02-01 00:00:00", obs.getObsDateTime());
        assertEquals("2016-11-09 7:31:43", obs.getDateCreated());
        assertEquals("8", obs.getLocationId());
        assertEquals("Registration", obs.getLocationName());
        assertEquals("1", obs.getProgramId());
        assertEquals("First Stage Validation", obs.getProgramName());
    }


    @Test
    public void shouldReturnObsWithParentFormFieldPathWhenFormDepthToParentIsZero() {
        BahmniForm form = mock(BahmniForm.class);
        when(form.getDepthToParent()).thenReturn(0);
        form2ObservationProcessor.setForm(form);
        Map<String, Object> obsRow = getObsRow();

        List<Obs> obsList = form2ObservationProcessor.process(obsRow);

        assertEquals(1, obsList.size());
        Obs obs = obsList.get(0);
        assertEquals("FormOne", obs.getFormFieldPath());
        assertNull(obs.getReferenceFormFieldPath());
    }

    @Test
    public void shouldReturnObsWithParentFormFieldPathAndReferenceFormFieldPathWhenFormDepthToParentIs2() {
        BahmniForm form = mock(BahmniForm.class);
        BahmniForm parentForm = mock(BahmniForm.class);
        when(parentForm.getDepthToParent()).thenReturn(0);
        when(form.getParent()).thenReturn(parentForm);
        when(form.getDepthToParent()).thenReturn(2);
        form2ObservationProcessor.setForm(form);
        Map<String, Object> obsRow = getObsRow();

        List<Obs> obsList = form2ObservationProcessor.process(obsRow);

        assertEquals(1, obsList.size());
        Obs obs = obsList.get(0);
        assertEquals("FormOne.1/3-0/7-0", obs.getFormFieldPath());
        assertEquals("FormOne", obs.getReferenceFormFieldPath());
    }


    @Test
    public void shouldReturnObsWithParentFormFieldPathAndReferenceFormFieldPathWhenParentFormDepthToParentIs1() {
        BahmniForm form = mock(BahmniForm.class);
        BahmniForm parentForm = mock(BahmniForm.class);
        when(parentForm.getDepthToParent()).thenReturn(1);
        when(form.getParent()).thenReturn(parentForm);
        when(form.getDepthToParent()).thenReturn(3);
        form2ObservationProcessor.setForm(form);
        Map<String, Object> obsRow = getObsRow();

        List<Obs> obsList = form2ObservationProcessor.process(obsRow);

        assertEquals(1, obsList.size());
        Obs obs = obsList.get(0);
        assertEquals("FormOne.1/3-0/7-0/2-1", obs.getFormFieldPath());
        assertEquals("FormOne.1/3-0", obs.getReferenceFormFieldPath());
    }

    @Test
    public void shouldReturnObsWithParentFormFieldPathAndReferenceFormFieldPathWhenFormIsMultiSelect() {
        BahmniForm form = mock(BahmniForm.class);
        BahmniForm parentForm = mock(BahmniForm.class);
        when(parentForm.getDepthToParent()).thenReturn(1);
        when(form.getParent()).thenReturn(parentForm);
        when(form.getDepthToParent()).thenReturn(4);
        form2ObservationProcessor.setForm(form);
        Map<String, Object> obsRow = getObsRow();

        List<Obs> obsList = form2ObservationProcessor.process(obsRow);

        assertEquals(1, obsList.size());
        Obs obs = obsList.get(0);
        assertEquals("FormOne.1/3-0/7-0/2-1/100-0", obs.getFormFieldPath());
        assertEquals("FormOne.1/3-0", obs.getReferenceFormFieldPath());
    }

    private Map<String, Object> getObsRow() {
        Map<String, Object> obsRow = new HashMap<>();
        obsRow.put("encounterId", "abc123");
        obsRow.put("patientId", "def789");
        obsRow.put("conceptId", 7);
        obsRow.put("id", 97);
        obsRow.put("value", "21");
        obsRow.put("conceptName", "age");
        obsRow.put("obsDateTime", "2015-02-01 00:00:00");
        obsRow.put("dateCreated", "2016-11-09 7:31:43");
        obsRow.put("locationId", "8");
        obsRow.put("locationName", "Registration");
        obsRow.put("programId", "1");
        obsRow.put("programName", "First Stage Validation");
        obsRow.put("formFieldPath", "FormOne.1/3-0/7-0/2-1/100-0");
        return obsRow;
    }
}
