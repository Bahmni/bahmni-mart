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
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RspConfigHelper.class, JsonParser.class, FileReader.class})
public class RspConfigHelperTest {
    @Mock
    private FileReader defaultFileReader;

    @Mock
    private FileReader implementationFileReader;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private Logger logger;

    private RspConfigHelper rspConfigHelper;

    private String jsonString = "{\n" +
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
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        rspConfigHelper = new RspConfigHelper();
        setValuesForMemberFields(rspConfigHelper, "defaultExtensionConfigFile", "default.json");
        setValuesForMemberFields(rspConfigHelper, "implementationExtensionConfigFile", "implementation.json");
        setValueForFinalStaticField(RspConfigHelper.class, "log", logger);

    }

    @Test
    public void shouldGiveAllRspConceptNames() throws Exception {
        String defaultJson = "{\n" +
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

        String defaultExtFilePath = "default.json";
        String implExtFilePath = "implementation.json";

        setValuesForMemberFields(rspConfigHelper, "defaultExtensionConfigFile", defaultExtFilePath);
        setValuesForMemberFields(rspConfigHelper, "implementationExtensionConfigFile", implExtFilePath);

        List<String> expected = Arrays.asList("Nutritional", "Fee Information", "Nutritional Temp");

        whenNew(FileReader.class).withArguments(defaultExtFilePath).thenReturn(defaultFileReader);
        whenNew(FileReader.class).withArguments(implExtFilePath).thenReturn(implementationFileReader);
        when(jsonParser.parse(defaultFileReader)).thenReturn(new JsonParser().parse(defaultJson));
        when(jsonParser.parse(implementationFileReader)).thenReturn(new JsonParser().parse(implJson));

        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);

        List<String> rspConcepts = rspConfigHelper.getRspConcepts();

        verify(jsonParser, times(1)).parse(defaultFileReader);
        verify(jsonParser, times(1)).parse(implementationFileReader);
        assertEquals(3, rspConcepts.size());
        assertTrue(rspConcepts.containsAll(expected));
    }

    @Test
    public void shouldGiveEmptyListAsRspConcept() throws Exception {
        whenNew(FileReader.class).withAnyArguments().thenReturn(defaultFileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);

        when(jsonParser.parse(defaultFileReader)).thenReturn(new JsonObject());

        List<String> rspConcepts = rspConfigHelper.getRspConcepts();
        assertTrue(rspConcepts.isEmpty());
    }

    @Test
    public void shouldGiveEmptyListWhenBothFilesIsNotPresent() throws Exception {
        whenNew(FileReader.class).withArguments("default.json").thenThrow(new FileNotFoundException());
        whenNew(FileReader.class).withArguments("implementation.json").thenThrow(new FileNotFoundException());
        List<String> rspConcepts = rspConfigHelper.getRspConcepts();
        verify(logger, times(2)).warn(any(), any(FileNotFoundException.class));
        assertTrue(rspConcepts.isEmpty());
    }

    @Test
    public void shouldGiveAllRspConceptNamesEvenIfImplementationJsonFileIsNotPresent() throws Exception {
        whenNew(FileReader.class).withArguments("default.json").thenReturn(defaultFileReader);
        whenNew(FileReader.class).withArguments("implementation.json").thenThrow(new FileNotFoundException());
        JsonElement jsonConfig = new JsonParser().parse(jsonString);
        List<String> expected = Arrays.asList("Nutritional Values", "Fee Information");

        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(defaultFileReader)).thenReturn(jsonConfig);

        List<String> rspConcepts = rspConfigHelper.getRspConcepts();

        verify(jsonParser, times(1)).parse(defaultFileReader);
        assertEquals(2, rspConcepts.size());
        assertTrue(rspConcepts.containsAll(expected));
        verify(logger, times(1)).warn(any(), any(FileNotFoundException.class));
    }

    @Test
    public void shouldGiveAllRspConceptNamesEvenIfDefaultJsonFileIsNotPresent() throws Exception {
        whenNew(FileReader.class).withArguments("implementation.json").thenReturn(defaultFileReader);
        whenNew(FileReader.class).withArguments("default.json").thenThrow(new FileNotFoundException());
        JsonElement jsonConfig = new JsonParser().parse(jsonString);
        List<String> expected = Arrays.asList("Nutritional Values", "Fee Information");

        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(defaultFileReader)).thenReturn(jsonConfig);

        List<String> rspConcepts = rspConfigHelper.getRspConcepts();

        verify(jsonParser, times(1)).parse(defaultFileReader);
        assertEquals(2, rspConcepts.size());
        assertTrue(rspConcepts.containsAll(expected));
        verify(logger, times(1)).warn(any(), any(FileNotFoundException.class));
    }


}