package org.bahmni.mart.form2.translations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;

import static org.junit.Assert.assertEquals;
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
    public void shouldReturnTranslationsFileWhenFormNameAndVersionAreGiven() {

        TranslationMetadata translationMetadata = getTranslationMetadata();
        File file = translationMetadata.getTransaltionsFile("Vitals", 2);

        assertEquals("/home/bahmni/clinical_forms/translations/Vitals_2.json", file.getPath());
        assertEquals("Vitals_2.json", file.getName());
    }

    @Test
    public void shouldReturnTranslationsFileEnsureThatReplaceAllSpacesToUnderlineOfTheFileName() {

        String formName = " Test It    abcdefg ";

        File file = translationMetadata.getTransaltionsFile(formName, 1);

        assertEquals("/home/bahmni/clinical_forms/translations/_Test_It____abcdefg__1.json", file.getPath());
        assertEquals("_Test_It____abcdefg__1.json", file.getName());
    }

    @Test
    public void shouldReturnTranslationsFileEnsureThatReplaceAllSpecialCharactersToUnderlineOfTheFileName() {

        String formName = "Test&a*b@c%d111(8`9。，；'｀。.～！＃$^)-=+\\/\":啊";

        File file = translationMetadata.getTransaltionsFile(formName, 1);

        assertEquals("/home/bahmni/clinical_forms/translations/Test_a_b_c_d111_8_9______.______-________1.json",
                file.getPath());
        assertEquals("Test_a_b_c_d111_8_9______.______-________1.json", file.getName());
    }

    @Test
    public void shouldReturnTranslationsFileWithFormUuidWhenFormNameAndVersionAreGiven() {

        String queryForUUID = String.format("SELECT form.uuid FROM form WHERE version = %d AND form.name = \"%s\"",
                2, "Vitals");

        when(openmrsJdbcTemplate.queryForObject(queryForUUID, String.class))
                .thenReturn("91770617-a6d0-4ad4-a0a2-c77bd5926bd2");

        File file = translationMetadata.getTransaltionsFile("Vitals", 2);
        assertEquals("/home/bahmni/clinical_forms/translations/91770617-a6d0-4ad4-a0a2-c77bd5926bd2.json",
                file.getPath());
        assertEquals("91770617-a6d0-4ad4-a0a2-c77bd5926bd2.json",
                file.getName());
    }

    private TranslationMetadata getTranslationMetadata() {

        String formTranslationsFileLocation = "/home/bahmni/clinical_forms/translations";
        when(openmrsJdbcTemplate.queryForObject("SELECT property_value FROM global_property " +
                "WHERE property = 'bahmni.formTranslations.directory'", String.class))
                .thenReturn(formTranslationsFileLocation);

        return new TranslationMetadata(openmrsJdbcTemplate);
    }
}
