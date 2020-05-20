package org.bahmni.mart.form2.translations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class TranslationMetadataTest {

    private TranslationMetadata translationMetadata;

    @Mock
    private JdbcTemplate openmrsJdbcTemplate;

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

    @Test
    public void shouldReturnTranslationsFilePathWithFormUuidWhenFormNameAndVersionAreGiven() {

        String queryForUUID = String.format("SELECT form.uuid FROM form WHERE version = %d AND form.name = \"%s\"", 2, "Vitals");

        when(openmrsJdbcTemplate.queryForObject(queryForUUID, String.class)).thenReturn("91770617-a6d0-4ad4-a0a2-c77bd5926bd2");

        String formTranslationsPath = translationMetadata.getTranslationsFilePathWithUuid("Vitals", 2);
        assertEquals("/home/bahmni/clinical_forms/translations/91770617-a6d0-4ad4-a0a2-c77bd5926bd2.json", formTranslationsPath);
    }

    private TranslationMetadata getTranslationMetadata() {

        String formTranslationsFileLocation = "/home/bahmni/clinical_forms/translations";
        when(openmrsJdbcTemplate.queryForObject("SELECT property_value FROM global_property " +
                "WHERE property = 'bahmni.formTranslations.directory'", String.class))
                .thenReturn(formTranslationsFileLocation);

        return new TranslationMetadata(openmrsJdbcTemplate);
    }
}
