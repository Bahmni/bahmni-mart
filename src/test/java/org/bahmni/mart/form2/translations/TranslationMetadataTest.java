package org.bahmni.mart.form2.translations;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TranslationMetadataTest {

    private TranslationMetadata translationMetadata;

    @Before
    public void setup() {
        translationMetadata = getTranslationMetadata();
    }

    @Test
    public void shouldReturnTranslationsFilePathWhenFormNameAndVersionAreGiven() {

        TranslationMetadata translationMetadata = getTranslationMetadata();
        String formTranslationsPath = translationMetadata.getTranslationsFilePath("Vitals", 2);

        assertEquals("/home/bahmni/clinical_forms/translations/Vitals_2.json", formTranslationsPath);
    }

    @Test
    public void shouldReturnTranslationsFilePathEnsureThatReplaceAllSpacesToUnderlineOfTheFileName() {

        String fileName = " Test It    abcdefg ";
        fileName = translationMetadata.getNormalizedTranslationsFilePath(fileName, 1);
        assertEquals("/home/bahmni/clinical_forms/translations/_Test_It____abcdefg__1.json", fileName);
    }

    @Test
    public void shouldReturnTranslationsFilePathEnsureThatReplaceAllSpecialCharactersToUnderlineOfTheFileName() {

        String fileName = "Test&a*b@c%d111(8`9。，；'｀。.～！＃$^)-=+\\/\":啊";

        fileName = translationMetadata.getNormalizedTranslationsFilePath(fileName, 1);

        assertEquals("/home/bahmni/clinical_forms/translations/Test_a_b_c_d111_8_9______.______-________1.json",
                fileName);
    }

    private TranslationMetadata getTranslationMetadata() {
        JdbcTemplate openmrsJdbcTemplate = mock(JdbcTemplate.class);
        String formTranslationsFileLocation = "/home/bahmni/clinical_forms/translations";
        when(openmrsJdbcTemplate.queryForObject("SELECT property_value FROM global_property " +
                "WHERE property = 'bahmni.formTranslations.directory'", String.class))
                .thenReturn(formTranslationsFileLocation);

        return new TranslationMetadata(openmrsJdbcTemplate);
    }

}
