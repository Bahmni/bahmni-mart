package org.bahmni.mart.form2.translations;

import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TranslationMetadataTest {

    @Test
    public void shouldReturnTranslationsFilePathWhenFormNameAndVersionAreGiven() {

        JdbcTemplate openmrsJdbcTemplate = mock(JdbcTemplate.class);
        String formTranslationsFileLocation = "/home/bahmni/clinical_forms/translations";
        when(openmrsJdbcTemplate.queryForObject("SELECT property_value FROM global_property " +
                "WHERE property = 'bahmni.formTranslations.directory'", String.class))
                .thenReturn(formTranslationsFileLocation);

        TranslationMetadata translationMetadata = new TranslationMetadata(openmrsJdbcTemplate);
        String formTranslationsPath = translationMetadata.getTranslationsFilePath("Vitals", 2);

        assertEquals("/home/bahmni/clinical_forms/translations/Vitals_2.json", formTranslationsPath);
    }
}