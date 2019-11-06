package org.bahmni.mart.form2.translations.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.form2.translations.TranslationException;
import org.bahmni.mart.form2.translations.TranslationMetadata;
import org.bahmni.mart.form2.translations.model.Form2Translation;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@Component
public class Form2TranslationsReader {

    private static final Logger logger = LoggerFactory.getLogger(Form2TranslationsReader.class);

    private final TranslationMetadata translationMetadata;

    @Autowired
    public Form2TranslationsReader(TranslationMetadata translationMetadata) {
        this.translationMetadata = translationMetadata;
    }

    public Form2Translation read(String formName, int version, String locale) {
        JSONObject jsonObject = getTranslationsAsJSONObject(formName, version);

        return TranslationsParser.parse(jsonObject, locale);
    }

    public String getTranslation(Form2Translation form2Translation, String translationKey) {
        if (form2Translation == null || StringUtils.isBlank(translationKey))
            throw new TranslationException();
        Map<String, String> conceptTranslations = form2Translation.getConcepts();
        Map<String, String> labelTranslations = form2Translation.getLabels();
        return conceptTranslations.containsKey(translationKey) ? conceptTranslations.get(translationKey)
                : labelTranslations.get(translationKey);
    }

    private JSONObject getTranslationsAsJSONObject(String formName, int version) {
        String translationsFilePath = translationMetadata.getTranslationsFilePath(formName, version);
        File translationsFile = new File(translationsFilePath);
        if(!translationsFile.exists())
            translationsFile = new File(translationMetadata.getNormalizedTranslationsFilePath(formName,version));
        return new JSONObject(getTranslationsAsString(translationsFile));
    }

    private String getTranslationsAsString(File translationsFile) {
        String translations = "{}";
        try {
            translations = FileUtils.readFileToString(translationsFile);
        } catch (FileNotFoundException e) {
            logger.warn(String.format("Translations file '%s' does not exist.", translationsFile));
        } catch (IOException e) {
            logger.warn(String.format("Unable to read the translations file '%s'.", translationsFile), e);
        }
        return translations;
    }
}
