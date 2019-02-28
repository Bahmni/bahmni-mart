package org.bahmni.mart.form2.translations.util;

import org.bahmni.mart.form2.translations.model.Form2Translation;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

public class TranslationsParserTest {

    @Test
    public void shouldCreateForm2TranslationFromGivenJsonObjectAndLocaleEnglish() {
        String translationsJson = "{\"en\":{\"concepts\":{\"TEMPERATURE_1\":\"Temperature\",\"TEMPERATURE_1_DESC\":" +
                "\"Temperature Desc\"},\"labels\":{\"LABEL_2\":\"Vitals\"}}}";
        JSONObject translation = new JSONObject(translationsJson);

        Form2Translation form2Translation = TranslationsParser.parse(translation, "en");

        assertEquals("en", form2Translation.getLocale());
        assertEquals(2, form2Translation.getConcepts().size());
        assertEquals(1, form2Translation.getLabels().size());
        assertEquals("Temperature", form2Translation.getConcepts().get("TEMPERATURE_1"));
        assertEquals("Temperature Desc", form2Translation.getConcepts().get("TEMPERATURE_1_DESC"));
        assertEquals("Vitals", form2Translation.getLabels().get("LABEL_2"));
    }

    @Test
    public void shouldCreateForm2TranslationFromGivenJsonObjectAndLocaleFrench() {
        String translationsJson = "{\"en\":{\"concepts\":{\"TEMPERATURE_1\":\"Temperature\",\"TEMPERATURE_1_DESC\":" +
                "\"Temperature Desc\"},\"labels\":{\"LABEL_2\":\"Vitals\"}}," +
                "\"fr\":{\"concepts\":{\"TEMPERATURE_1\":\"Température\",\"TEMPERATURE_1_DESC\":" +
                "\"Température Desc\"},\"labels\":{\"LABEL_2\":\"Vitals\"}}}";
        JSONObject translation = new JSONObject(translationsJson);

        Form2Translation form2Translation = TranslationsParser.parse(translation, "fr");

        assertEquals("fr", form2Translation.getLocale());
        assertEquals(2, form2Translation.getConcepts().size());
        assertEquals(1, form2Translation.getLabels().size());
        assertEquals("Température", form2Translation.getConcepts().get("TEMPERATURE_1"));
        assertEquals("Température Desc", form2Translation.getConcepts().get("TEMPERATURE_1_DESC"));
        assertEquals("Vitals", form2Translation.getLabels().get("LABEL_2"));
    }


    @Test
    public void shouldReturnEmptyTranslationsParserObjectForUnknownLocale() throws Exception {
        String translationsJson = "{\"en\":{\"concepts\":{\"TEMPERATURE_1\":\"Temperature\",\"TEMPERATURE_1_DESC\":" +
                "\"Temperature Desc\"},\"labels\":{\"LABEL_2\":\"Vitals\"}}}";
        JSONObject translation = new JSONObject(translationsJson);

        Form2Translation form2Translation = TranslationsParser.parse(translation, "abc");

        assertNull(form2Translation.getLabels());
        assertNull(form2Translation.getConcepts());
        assertNull(form2Translation.getLocale());
    }

    @Test
    public void shouldLogWarningWhenNoTranslationsAvailableForConceptsInTranslationJson() throws Exception {
        String translationsJson = "{\"en\":{\"labels\":{\"LABEL_2\":\"Vitals\"}}}";
        JSONObject translation = new JSONObject(translationsJson);

        Logger logger = Mockito.mock(Logger.class);
        setValueForFinalStaticField(TranslationsParser.class, "logger", logger);
        TranslationsParser.parse(translation, "en");

        verify(logger, Mockito.times(1)).warn("JSONObject[\"concepts\"] not found.");
    }

    @Test
    public void shouldLogWarningWhenNoTranslationsAvailableForLabelsInTranslationJson() throws Exception {
        String translationsJson = "{\"en\":{\"concepts\":{\"TEMPERATURE_1\":\"Temperature\",\"TEMPERATURE_1_DESC\":" +
                "\"Temperature Desc\"}}}";
        JSONObject translation = new JSONObject(translationsJson);

        Logger logger = Mockito.mock(Logger.class);
        setValueForFinalStaticField(TranslationsParser.class, "logger", logger);
        TranslationsParser.parse(translation, "en");

        verify(logger, Mockito.times(1)).warn("JSONObject[\"labels\"] not found.");
    }

    @Test
    public void shouldReturnZeroTranslationsIfTranslationsJsonDoesNotHaveAny() throws Exception {
        String translationsJson = "{\"en\":{}}";
        JSONObject translation = new JSONObject(translationsJson);

        Form2Translation form2Translation = TranslationsParser.parse(translation, "en");
        assertEquals(0, form2Translation.getConcepts().size());
        assertEquals(0, form2Translation.getLabels().size());
    }

}