package org.bahmni.mart.form2.translations.util;

import org.bahmni.mart.form2.translations.model.Form2Translation;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class TranslationsParser {

    private static final Logger logger = LoggerFactory.getLogger(TranslationsParser.class);

    private static String CONCEPTS = "concepts";
    private static String LABELS = "labels";

    public static Form2Translation parse(JSONObject jsonObject, String locale) {
        Form2Translation formTranslation = new Form2Translation();
        if (!jsonObject.has(locale))
            return formTranslation;
        JSONObject translations = (JSONObject) jsonObject.get(locale);

        JSONObject conceptsObj = new JSONObject();
        JSONObject labelsObj = new JSONObject();
        try {
            conceptsObj = (JSONObject) translations.get(CONCEPTS);
            labelsObj = (JSONObject) translations.get(LABELS);
        } catch (JSONException jsonException) {
            logger.warn(jsonException.getLocalizedMessage());
        }
        Map<String, String> concepts = conceptsObj.keySet().stream()
                .collect(Collectors.toMap(translationKey -> translationKey, conceptsObj::getString));
        Map<String, String> labels = labelsObj.keySet().stream()
                .collect(Collectors.toMap(translationKey -> translationKey, labelsObj::getString));

        formTranslation.setLocale(locale);
        formTranslation.setConcepts(concepts);
        formTranslation.setLabels(labels);
        return formTranslation;
    }
}
