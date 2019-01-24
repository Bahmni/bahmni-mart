package org.bahmni.mart.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RegConfigHelper.class, JsonParser.class, FileReader.class})
public class RegConfigHelperTest {
    @Mock
    private FileReader defaultFileReader;

    @Mock
    private FileReader implementationFileReader;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private Logger logger;

    private RegConfigHelper regConfigHelper;

    private String defaultExtFilePath = "default.json";
    private String implExtFilePath = "implementation.json";

    private String jsonString = "{\n" +
            "\"shouldOverRideConfig\": true," +
            "  \"view\": {\n" +
            "    \"extensionPointId\": \"org.bahmni.registration.patient.search.result.action\",\n" +
            "    \"type\": \"link\"\n" +
            "  },\n" +
            "  \"nutritionalValues\": {\n" +
            "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
            "    \"type\": \"config\",\n" +
            "    \"extensionParams\": {\n" +
            "      \"conceptName\": \"Nutritional Values\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"feeInformation\": {\n" +
            "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
            "    \"type\": \"config\",\n" +
            "    \"extensionParams\": {\n" +
            "      \"conceptName\": \"Fee Information\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"nutritionalValue\": {\n" +
            "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
            "    \"type\": \"forms\",\n" +
            "    \"extensionParams\": {\n" +
            "      \"formName\": \"Nutritional Values\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @Before
    public void setUp() throws Exception {
        regConfigHelper = new RegConfigHelper();
        setValuesForMemberFields(regConfigHelper, "defaultExtensionConfigFile", "default.json");
        setValuesForMemberFields(regConfigHelper, "implementationExtensionConfigFile", "implementation.json");
        setValueForFinalStaticField(RegConfigHelper.class, "log", logger);

    }

    @Test
    public void shouldGiveAllRegConceptNames() throws Exception {
        String defaultJson = "{\n" +
                "\"shouldOverRideConfig\": true," +
                "  \"nutritionalValues\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"config\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Nutritional Values\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"feeInformation\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"config\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Fee Information\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"nutritionalValueTemp\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"config\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Nutritional Temp\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String implJson = "{\n" +
                "  \"nutritionalValues\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"form\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Nutritional Values\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"feeInformation\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"config\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Fee Information\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"nutritionalValue\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"config\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Nutritional\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        setValuesForMemberFields(regConfigHelper, "defaultExtensionConfigFile", defaultExtFilePath);
        setValuesForMemberFields(regConfigHelper, "implementationExtensionConfigFile", implExtFilePath);

        List<String> expected = Arrays.asList("Nutritional", "Fee Information", "Nutritional Temp");

        whenNew(FileReader.class).withArguments(defaultExtFilePath).thenReturn(defaultFileReader);
        whenNew(FileReader.class).withArguments(implExtFilePath).thenReturn(implementationFileReader);
        when(jsonParser.parse(defaultFileReader)).thenReturn(new JsonParser().parse(defaultJson));
        when(jsonParser.parse(implementationFileReader)).thenReturn(new JsonParser().parse(implJson));

        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);

        List<String> regConcepts = regConfigHelper.getRegConcepts();

        verify(jsonParser, times(2)).parse(defaultFileReader);
        verify(jsonParser, times(1)).parse(implementationFileReader);
        assertEquals(3, regConcepts.size());
        assertTrue(regConcepts.containsAll(expected));
    }

    @Test
    public void shouldGiveEmptyListAsRegConcept() throws Exception {
        whenNew(FileReader.class).withAnyArguments().thenReturn(defaultFileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);

        when(jsonParser.parse(defaultFileReader)).thenReturn(new JsonObject());

        List<String> regConcepts = regConfigHelper.getRegConcepts();
        assertTrue(regConcepts.isEmpty());
    }

    @Test
    public void shouldGiveEmptyListWhenBothFilesIsNotPresent() throws Exception {
        whenNew(FileReader.class).withArguments(defaultExtFilePath).thenThrow(new FileNotFoundException());
        whenNew(FileReader.class).withArguments(implExtFilePath).thenThrow(new FileNotFoundException());
        List<String> regConcepts = regConfigHelper.getRegConcepts();
        verify(logger, times(2)).warn(any(), any(FileNotFoundException.class));
        assertTrue(regConcepts.isEmpty());
    }

    @Test
    public void shouldGiveAllRegConceptNamesEvenIfImplementationJsonFileIsNotPresent() throws Exception {
        whenNew(FileReader.class).withArguments(defaultExtFilePath).thenReturn(defaultFileReader);
        whenNew(FileReader.class).withArguments(implExtFilePath).thenThrow(new FileNotFoundException());
        JsonElement jsonConfig = new JsonParser().parse(jsonString);
        List<String> expected = Arrays.asList("Nutritional Values", "Fee Information");

        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(defaultFileReader)).thenReturn(jsonConfig);

        List<String> regConcepts = regConfigHelper.getRegConcepts();

        verify(jsonParser, times(2)).parse(defaultFileReader);
        assertEquals(2, regConcepts.size());
        assertTrue(regConcepts.containsAll(expected));
        verify(logger, times(1)).warn(any(), any(FileNotFoundException.class));
    }

    @Test
    public void shouldGiveAllRegConceptNamesEvenIfDefaultJsonFileIsNotPresent() throws Exception {
        whenNew(FileReader.class).withArguments(defaultExtFilePath).thenThrow(new FileNotFoundException());
        whenNew(FileReader.class).withArguments(implExtFilePath).thenReturn(defaultFileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);

        List<String> regConcepts = regConfigHelper.getRegConcepts();

        verify(jsonParser, never()).parse(defaultFileReader);
        assertTrue(regConcepts.isEmpty());
        verify(logger, times(2)).warn(any(), any(FileNotFoundException.class));
    }

    @Test
    public void shouldMergeImplementationAndDefaultConfigs() throws Exception {
        String defaultJson = "{\n" +
                "\"shouldOverRideConfig\": true," +
                "  \"nutritionalValues\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"config\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Nutritional Values\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String implJson = "{\n" +
                "  \"feeInformation\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"config\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Fee Information\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        List<String> expected = Arrays.asList("Fee Information", "Nutritional Values");

        setValuesForMemberFields(regConfigHelper, "defaultExtensionConfigFile", defaultExtFilePath);
        setValuesForMemberFields(regConfigHelper, "implementationExtensionConfigFile", implExtFilePath);


        whenNew(FileReader.class).withArguments(defaultExtFilePath).thenReturn(defaultFileReader);
        whenNew(FileReader.class).withArguments(implExtFilePath).thenReturn(implementationFileReader);
        when(jsonParser.parse(defaultFileReader)).thenReturn(new JsonParser().parse(defaultJson));
        when(jsonParser.parse(implementationFileReader)).thenReturn(new JsonParser().parse(implJson));

        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);

        assertEquals(expected, regConfigHelper.getRegConcepts());
        verify(jsonParser, times(2)).parse(defaultFileReader);
        verify(jsonParser, times(1)).parse(implementationFileReader);
    }

    @Test
    public void shouldPrioritizeImplementationConfigOverDefaultConfigWhileMerging() throws Exception {
        String defaultJson = "{\n" +
                "\"shouldOverRideConfig\": true,\n" +
                "  \"nutritionalValues\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.temp\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Nutritional Values\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String implJson = "{\n" +
                "  \"nutritionalValues\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"config\"\n" +
                "  }\n" +
                "}";

        setValuesForMemberFields(regConfigHelper, "defaultExtensionConfigFile", defaultExtFilePath);
        setValuesForMemberFields(regConfigHelper, "implementationExtensionConfigFile", implExtFilePath);

        List<String> expected = Collections.singletonList("Nutritional Values");

        whenNew(FileReader.class).withArguments(defaultExtFilePath).thenReturn(defaultFileReader);
        whenNew(FileReader.class).withArguments(implExtFilePath).thenReturn(implementationFileReader);
        when(jsonParser.parse(defaultFileReader)).thenReturn(new JsonParser().parse(defaultJson));
        when(jsonParser.parse(implementationFileReader)).thenReturn(new JsonParser().parse(implJson));

        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);

        assertEquals(expected, regConfigHelper.getRegConcepts());
        verify(jsonParser, times(2)).parse(defaultFileReader);
        verify(jsonParser, times(1)).parse(implementationFileReader);
    }

    @Test
    public void shouldNotOverrideConfigIfShouldOverrideIsSetToFalse() throws Exception {
        String defaultJson = "{\n" +
                "  \"nutritionalValues\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.temp\",\n" +
                "    \"extensionParams\": {\n" +
                "      \"conceptName\": \"Nutritional Values\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String implJson = "{\n" +
                "  \"nutritionalValues\": {\n" +
                "    \"extensionPointId\": \"org.bahmni.registration.conceptSetGroup.observations\",\n" +
                "    \"type\": \"config\"\n" +
                "  }\n" +
                "}";

        setValuesForMemberFields(regConfigHelper, "defaultExtensionConfigFile", defaultExtFilePath);
        setValuesForMemberFields(regConfigHelper, "implementationExtensionConfigFile", implExtFilePath);

        List<String> expected = Collections.emptyList();

        whenNew(FileReader.class).withArguments(defaultExtFilePath).thenReturn(defaultFileReader);
        whenNew(FileReader.class).withArguments(implExtFilePath).thenReturn(implementationFileReader);
        when(jsonParser.parse(defaultFileReader)).thenReturn(new JsonParser().parse(defaultJson));
        when(jsonParser.parse(implementationFileReader)).thenReturn(new JsonParser().parse(implJson));

        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);

        assertEquals(expected, regConfigHelper.getRegConcepts());
        verify(jsonParser, times(2)).parse(defaultFileReader);
        verify(jsonParser, never()).parse(implementationFileReader);
    }
}
