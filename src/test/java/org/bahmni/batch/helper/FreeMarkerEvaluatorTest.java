package org.bahmni.batch.helper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.bahmni.batch.exception.BatchResourceException;
import org.bahmni.batch.form.domain.BahmniForm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mock;

@PrepareForTest({FreeMarkerEvaluator.class})
@RunWith(PowerMockRunner.class)
public class FreeMarkerEvaluatorTest {

    FreeMarkerEvaluator freeMarkerEvaluator;

    @Mock
    Configuration configuration;

    @Rule
    ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        freeMarkerEvaluator = new FreeMarkerEvaluator();
        setValuesForMemberFields(freeMarkerEvaluator, "configuration", configuration);
    }

    @Test
    public void shouldEvaluateTemplate() throws Exception {
        BahmniForm bahmniForm = new BahmniForm();
        String templateName = "Vital Signs";
        StringWriter stringWriter = Mockito.mock(StringWriter.class);
        PowerMockito.whenNew(StringWriter.class).withNoArguments().thenReturn(stringWriter);
        Template template = Mockito.mock(Template.class);
        Mockito.when(configuration.getTemplate(templateName)).thenReturn(template);
        HashMap<String, Object> hashMap = mock(HashMap.class);
        PowerMockito.whenNew(HashMap.class).withNoArguments().thenReturn(hashMap);
        String expectedOutput = "outputValue";
        Mockito.when(stringWriter.toString()).thenReturn(expectedOutput);

        String actualOutput = freeMarkerEvaluator.evaluate(templateName, bahmniForm);

        Assert.assertEquals(expectedOutput, actualOutput);
        Mockito.verify(configuration, times(1)).getTemplate(templateName);
        Mockito.verify(hashMap, times(1)).put("input", bahmniForm);
        Mockito.verify(template, times(1)).process(hashMap, stringWriter);
    }

    @Test
    public void shouldThrowBatchResourceException() throws Exception {
        BahmniForm bahmniForm = new BahmniForm();
        String templateName = "Vital Signs";
        BatchResourceException batchResourceException = Mockito.mock(BatchResourceException.class);
        Mockito.when(configuration.getTemplate(templateName)).thenThrow(batchResourceException);

        expectedException.expect(BatchResourceException.class);
        expectedException.expectMessage("Unable to continue generating a the template with name ["
                + templateName + "]");

        freeMarkerEvaluator.evaluate(templateName, bahmniForm);
        Mockito.verify(configuration, times(1)).getTemplate(templateName);
    }

    private void setValuesForMemberFields(
            Object batchConfiguration, String fieldName, Object valueForMemberField)
            throws NoSuchFieldException, IllegalAccessException {
        Field f1 = batchConfiguration.getClass().getDeclaredField(fieldName);
        f1.setAccessible(true);
        f1.set(batchConfiguration, valueForMemberField);
    }
}
