package org.bahmni.mart.form2.translations.util;

import org.apache.commons.io.FileUtils;
import org.bahmni.mart.form2.translations.TranslationException;
import org.bahmni.mart.form2.translations.TranslationMetadata;
import org.bahmni.mart.form2.translations.model.Form2Translation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest(FileUtils.class)
@RunWith(PowerMockRunner.class)
public class Form2TranslationsReaderTest {

    String formName = "Vitals";
    int formVersion = 2;
    String locale = "fr";
    private TranslationMetadata translationMetadata;
    private Form2TranslationsReader form2TranslationsReader;
    private String tempTranslationFolderPath;
    @Rule
    private ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException, IOException {
        translationMetadata = mock(TranslationMetadata.class);
        form2TranslationsReader = new Form2TranslationsReader(translationMetadata);
        mockStatic(FileUtils.class);
        createTempFolder();
    }


    private void createTempFolder() throws IOException, NoSuchFieldException, IllegalAccessException {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        String translationsPath = temporaryFolder.getRoot().getAbsolutePath();
        tempTranslationFolderPath = translationsPath;
    }

    private File createFile(String fileName) throws IOException {
        File file = new File(tempTranslationFolderPath + "/" + fileName);
        file.createNewFile();
        return file;
    }

    private File createFileWithUuid(String uuidFileName) throws IOException {
        File file = new File(tempTranslationFolderPath + "/" + uuidFileName);
        file.createNewFile();
        return file;
    }


    @Test
    public void shouldReturnForm2TranslationsForGivenFormNameVersionAndLocale() throws IOException {

        String formName = "Vitals_2.json";
        String translationsAsString = "{\n" +
                "  \"en\": {\n" +
                "    \"concepts\": {\n" +
                "      \"HEIGHT_1\": \"Height\",\n" +
                "      \"WEIGHT_1\": \"Weight\",\n" +
                "    },\n" +
                "    \"labels\": {\n" +
                "      \"SECTION_1\": \"Readings\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"fr\": {\n" +
                "    \"concepts\": {\n" +
                "      \"HEIGHT_1\": \"la taille\",\n" +
                "      \"WEIGHT_1\": \"Poids\",\n" +
                "    },\n" +
                "    \"labels\": {\n" +
                "      \"SECTION_1\": \"Lectures\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        when(translationMetadata.getTranslationsFileWithUuid(formName, formVersion))
                .thenReturn(new File(""));
        when(translationMetadata.getTranslationsFileWithFormName(formName, formVersion))
                .thenReturn(createFile(formName));
        when(readFileToString(any(File.class))).thenReturn(translationsAsString);

        Form2Translation form2Translations = form2TranslationsReader.read(formName, formVersion, locale);

        assertEquals(2, form2Translations.getConcepts().size());
        assertEquals(1, form2Translations.getLabels().size());
        assertEquals("la taille", form2Translations.getConcepts().get("HEIGHT_1"));
        assertEquals("Poids", form2Translations.getConcepts().get("WEIGHT_1"));
        assertEquals("Lectures", form2Translations.getLabels().get("SECTION_1"));

    }

    @Test
    public void shouldReturnForm2TranslationsForGivenSpecialCharacterFormNameVersionAndLocaleWithNormalizedFilePath()
            throws IOException {
        String formNameWithSpecialCharacters = "2 Vitãls spéciælity_2.json";
        String normalizedFileName = "2_Vit_ls_sp_ci_lity_2.json";
        File invalidFile = new File("");
        String translationsAsString = "{\n" +
                "  \"en\": {\n" +
                "    \"concepts\": {\n" +
                "      \"HEIGHT_1\": \"Height\",\n" +
                "      \"WEIGHT_1\": \"Weight\",\n" +
                "    },\n" +
                "    \"labels\": {\n" +
                "      \"SECTION_1\": \"Readings\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"fr\": {\n" +
                "    \"concepts\": {\n" +
                "      \"HEIGHT_1\": \"la taille\",\n" +
                "      \"WEIGHT_1\": \"Poids\",\n" +
                "    },\n" +
                "    \"labels\": {\n" +
                "      \"SECTION_1\": \"Lectures\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        when(translationMetadata.getTranslationsFileWithUuid(formNameWithSpecialCharacters, formVersion))
                .thenReturn(invalidFile);
        when(translationMetadata.getTranslationsFileWithFormName(formNameWithSpecialCharacters, formVersion))
                .thenReturn(invalidFile);
        when(translationMetadata.getNormalizedTranslationsFile(formNameWithSpecialCharacters, formVersion))
                .thenReturn(createFile(normalizedFileName));
        when(readFileToString(any(File.class))).thenReturn(translationsAsString);

        Form2Translation form2Translations = form2TranslationsReader.read(formNameWithSpecialCharacters,
                formVersion, locale);

        assertEquals(2, form2Translations.getConcepts().size());
        assertEquals(1, form2Translations.getLabels().size());
        assertEquals("la taille", form2Translations.getConcepts().get("HEIGHT_1"));
        assertEquals("Poids", form2Translations.getConcepts().get("WEIGHT_1"));
        assertEquals("Lectures", form2Translations.getLabels().get("SECTION_1"));

        InOrder inOrder = inOrder(translationMetadata);
        inOrder.verify(translationMetadata, times(1))
                .getTranslationsFileWithUuid(formNameWithSpecialCharacters, formVersion);
        inOrder.verify(translationMetadata, times(1))
                .getTranslationsFileWithFormName(formNameWithSpecialCharacters, formVersion);
        inOrder.verify(translationMetadata, times(1))
                .getNormalizedTranslationsFile(formNameWithSpecialCharacters, formVersion);
    }

    @Test
    public void shouldReturnForm2TranslationsForGivenFormNameVersionAndLocaleWhenTranslationsFileNameIsUuid()
            throws IOException {

        String formName = "Vitals_2.json";
        String uuidFileName = "\"91770617-a6d0-4ad4-a0a2-c77bd5926bd2.json\"";
        String translationsAsString = "{\n" +
                "  \"en\": {\n" +
                "    \"concepts\": {\n" +
                "      \"HEIGHT_1\": \"Height\",\n" +
                "      \"WEIGHT_1\": \"Weight\",\n" +
                "    },\n" +
                "    \"labels\": {\n" +
                "      \"SECTION_1\": \"Readings\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"fr\": {\n" +
                "    \"concepts\": {\n" +
                "      \"HEIGHT_1\": \"la taille\",\n" +
                "      \"WEIGHT_1\": \"Poids\",\n" +
                "    },\n" +
                "    \"labels\": {\n" +
                "      \"SECTION_1\": \"Lectures\"\n" +
                "    }\n" +
                "  }\n" +
                "}";


        when(translationMetadata.getTranslationsFileWithUuid(formName, formVersion))
                .thenReturn(createFileWithUuid(uuidFileName));
        when(readFileToString(any(File.class))).thenReturn(translationsAsString);

        Form2Translation form2Translations = form2TranslationsReader.read(formName, formVersion, locale);

        assertEquals(2, form2Translations.getConcepts().size());
        assertEquals(1, form2Translations.getLabels().size());
        assertEquals("la taille", form2Translations.getConcepts().get("HEIGHT_1"));
        assertEquals("Poids", form2Translations.getConcepts().get("WEIGHT_1"));
        assertEquals("Lectures", form2Translations.getLabels().get("SECTION_1"));

    }

    @Test
    public void shouldLogWarningIfTranslationsFileNotFound() throws Exception {
        File invalidFile = new File("abc/Vitals_2.json");

        when(translationMetadata.getTranslationsFileWithUuid(formName, formVersion))
                .thenReturn(invalidFile);
        when(translationMetadata.getTranslationsFileWithFormName(formName, formVersion))
                .thenReturn(invalidFile);
        when(translationMetadata.getNormalizedTranslationsFile(formName, formVersion))
                .thenReturn(invalidFile);
        when(readFileToString(any(File.class))).thenThrow(FileNotFoundException.class);
        Logger logger = mock(Logger.class);
        setValueForFinalStaticField(Form2TranslationsReader.class, "logger", logger);

        form2TranslationsReader.read(formName, formVersion, locale);

        verify(logger, times(1)).warn("Translations file 'abc/Vitals_2.json' does not exist.");

    }

    @Test
    public void shouldLogWarningForAnyIOException() throws Exception {
        File translationFile = new File("Vitals_2.json");

        when(translationMetadata.getTranslationsFileWithUuid(formName, formVersion))
                .thenReturn(translationFile);
        when(translationMetadata.getTranslationsFileWithFormName(formName, formVersion))
                .thenReturn(translationFile);
        when(translationMetadata.getNormalizedTranslationsFile(formName,formVersion))
                .thenReturn(translationFile);
        when(readFileToString(translationFile)).thenThrow(IOException.class);
        Logger logger = mock(Logger.class);
        setValueForFinalStaticField(Form2TranslationsReader.class, "logger", logger);

        form2TranslationsReader.read(formName, formVersion, locale);

        verify(logger, times(1)).warn(eq(String.format("Unable to read the translations file " +
                        "'Vitals_2.json'.")),
                any(IOException.class));

    }

    @Test
    public void shouldReturnTranslationForGivenConceptTranslationKey() {
        Form2Translation form2Translation = new Form2Translation();
        String conceptTranslationKey = "HEIGHT_1";
        String expectedTranslation = "Height";
        Map<String, String> conceptTranslationMap = new HashMap<>();
        conceptTranslationMap.put(conceptTranslationKey, expectedTranslation);
        form2Translation.setConcepts(conceptTranslationMap);

        String translation = form2TranslationsReader.getTranslation(form2Translation, conceptTranslationKey);

        assertEquals(expectedTranslation, translation);

    }

    @Test
    public void shouldReturnTranslationForGivenLabelTranslationKey() {
        Form2Translation form2Translation = new Form2Translation();
        String labelTranslationKey = "LABEL_1";
        String expectedTranslation = "Weight";
        Map<String, String> labelTranslationMap = new HashMap<>();
        labelTranslationMap.put(labelTranslationKey, expectedTranslation);
        form2Translation.setConcepts(Collections.EMPTY_MAP);
        form2Translation.setLabels(labelTranslationMap);

        String translation = form2TranslationsReader.getTranslation(form2Translation, labelTranslationKey);

        assertEquals(expectedTranslation, translation);

    }

    @Test
    public void shouldThrowTranslationExceptionIfForm2TranslationIsNull() {

        expectedException.expect(TranslationException.class);
        form2TranslationsReader.getTranslation(null, "translationKey");

    }

    @Test
    public void shouldThrowTranslationExceptionForEmptyTranslationKey() {

        expectedException.expect(TranslationException.class);
        form2TranslationsReader.getTranslation(new Form2Translation(), "");

    }
}
