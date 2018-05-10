package org.bahmni.mart.form;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForJob;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({JobDefinitionUtil.class})
@RunWith(PowerMockRunner.class)
public class FormListProcessorTest {

    @Mock
    private BahmniFormFactory bahmniFormFactory;

    @Mock
    private JobDefinition jobDefinition;

    private FormListProcessor formListProcessor;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        formListProcessor = new FormListProcessor();
        setValuesForMemberFields(formListProcessor, "bahmniFormFactory", bahmniFormFactory);
    }

    @Test
    public void shouldRetrieveAllForms() {
        Concept conceptA = new Concept(1, "a", 1);
        List<Concept> conceptList = Collections.singletonList(conceptA);

        BahmniForm a11 = new BahmniFormBuilder().withName("a11").build();
        BahmniForm a12 = new BahmniFormBuilder().withName("a12").build();
        BahmniForm a13 = new BahmniFormBuilder().withName("a13").build();


        BahmniForm b11 = new BahmniFormBuilder().withName("b11").build();
        BahmniForm b12 = new BahmniFormBuilder().withName("b12").build();
        BahmniForm b13 = new BahmniFormBuilder().withName("b13").build();

        BahmniForm a1 = new BahmniFormBuilder().withName("a1").withChild(a11).withChild(a12).withChild(a13).build();
        BahmniForm b1 = new BahmniFormBuilder().withName("b1").withChild(b11).withChild(b12).withChild(b13).build();

        BahmniForm a = new BahmniFormBuilder().withName("a").withChild(a1).withChild(b1).build();

        when(bahmniFormFactory.createForm(conceptA, null, jobDefinition)).thenReturn(a);
        when(getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(Collections.emptyList());

        List<BahmniForm> expected = Arrays.asList(a, a1, b1, a11, a12, a13, b11, b12, b13);

        List<BahmniForm> actual = formListProcessor.retrieveAllForms(conceptList, jobDefinition);

        assertEquals(expected.size(), actual.size());
        assertEquals(new HashSet(expected), new HashSet(actual));
        verifyStatic(times(1));
        getIgnoreConceptNamesForJob(jobDefinition);
    }

    @Test
    public void shouldRetrieveFormsDiscardingIgnoreConcepts() {
        Concept conceptA = new Concept(1, "formA", 1);
        Concept conceptB = new Concept(1, "formB", 1);
        List<Concept> conceptList = Arrays.asList(conceptA, conceptB);

        BahmniForm childFormOfA = new BahmniFormBuilder().withName("childFormOfA").build();
        BahmniForm childFormOfB = new BahmniFormBuilder().withName("childFormOfB").build();

        BahmniForm formA = new BahmniFormBuilder().withName("formA").withChild(childFormOfA).build();
        BahmniForm formB = new BahmniFormBuilder().withName("formB").withChild(childFormOfB).build();

        when(bahmniFormFactory.createForm(conceptA, null, jobDefinition)).thenReturn(formA);
        when(bahmniFormFactory.createForm(conceptB, null, jobDefinition)).thenReturn(formB);
        when(getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(Collections.singletonList("formB"));

        List<BahmniForm> expected = Arrays.asList(formA, childFormOfA);

        List<BahmniForm> actual = formListProcessor.retrieveAllForms(conceptList, jobDefinition);

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
        verifyStatic(times(1));
        getIgnoreConceptNamesForJob(jobDefinition);
    }
}
