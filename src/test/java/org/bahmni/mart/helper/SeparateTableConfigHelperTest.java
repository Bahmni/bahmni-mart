package org.bahmni.mart.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForObsJob;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({SeparateTableConfigHelper.class, BatchUtils.class, JsonParser.class, JobDefinitionUtil.class})
@RunWith(PowerMockRunner.class)
public class SeparateTableConfigHelperTest {

    @Mock
    private FileReader fileReader;

    @Mock
    private FileReader implementationFileReader;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    private SeparateTableConfigHelper separateTableConfigHelper;


    @Before
    public void setUp() throws Exception {
        separateTableConfigHelper = new SeparateTableConfigHelper();
        setValuesForMemberFields(separateTableConfigHelper, "jobDefinitionReader", jobDefinitionReader);
        mockStatic(JobDefinitionUtil.class);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList());
    }

    @Test
    public void shouldReturnListOfMultiSelectAndAddMore() throws Exception {
        String jsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"Video\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      },\n" +
                "      \"FSTG, Specialty determined by MLO\":{\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"OR, Operation performed\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        JsonElement jsonConfig = new JsonParser().parse(jsonString);
        List<String> expected = Arrays.asList("FSTG, Specialty determined by MLO", "OR, Operation performed", "Video");
        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "jobDefinitionReader", jobDefinitionReader);
        mockStatic(BatchUtils.class);
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(Arrays.asList());
        when(BatchUtils.convertConceptNamesToSet(null)).thenReturn(Collections.emptyList());
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertEquals(3, multiSelectAndAddMoreConceptNames.size());
        assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
        verifyStatic(times(1));
    }

    @Test
    public void shouldReturnListOfMultiSelectAndAddMoreDiscardingIgnoreConcepts() throws Exception {
        String jsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"All Observation Templates\": {\n" +
                "        \"showPanelView\": false\n" +
                "      },\n" +
                "      \"Video\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      },\n" +
                "      \"FSTG, Specialty determined by MLO\":{\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"Test Concept\":{\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"OR, Operation performed\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonElement jsonConfig = new JsonParser().parse(jsonString);

        List<String> expected = Arrays.asList("FSTG, Specialty determined by MLO", "OR, Operation performed");
        List<String> ignoreConceptsSet = Arrays.asList("Test Concept", "Video");
        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        String ignoreConcepts = "Test Concept, Video";
        mockStatic(BatchUtils.class);
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(ignoreConceptsSet);
        when(BatchUtils.convertConceptNamesToSet(ignoreConcepts)).thenReturn(ignoreConceptsSet);
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertEquals(2, multiSelectAndAddMoreConceptNames.size());
        assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
        verifyStatic(times(1));
    }

    @Test
    public void shouldReturnEmptyListIfConfigDoesNotHoldAnyMultiSelectAndAddMoreConcept() throws Exception {
        String jsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"All Observation Templates\": {\n" +
                "        \"showPanelView\": false\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonElement jsonConfig = new JsonParser().parse(jsonString);

        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        mockStatic(BatchUtils.class);
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(Arrays.asList());
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertTrue(multiSelectAndAddMoreConceptNames.isEmpty());
        verifyStatic(times(1));
    }

    @Test
    public void shouldReturnEmptyListWhenFileIsNotPresentInTheGivenPath() throws Exception {
        SeparateTableConfigHelper configSeparateTableConfigHelper = new SeparateTableConfigHelper();
        setValuesForMemberFields(configSeparateTableConfigHelper, "jobDefinitionReader", jobDefinitionReader);
        whenNew(FileReader.class).withArguments("test1.json").thenThrow(new FileNotFoundException());
        whenNew(FileReader.class).withArguments("test2.json").thenThrow(new FileNotFoundException());
        setValuesForMemberFields(configSeparateTableConfigHelper, "defaultConfigFile", "test1.json");
        setValuesForMemberFields(configSeparateTableConfigHelper, "implementationConfigFile", "test2.json");
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(Arrays.asList());
        assertTrue(configSeparateTableConfigHelper.getAddMoreAndMultiSelectConceptNames().isEmpty());
    }

    @Test
    public void shouldReturnListOfMultiSelectAndAddMoreDiscardingIgnoreConceptsEvenIfDefaultConfigFileIsMissing()
            throws Exception {
        String jsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"All Observation Templates\": {\n" +
                "        \"showPanelView\": false\n" +
                "      },\n" +
                "      \"Video\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      },\n" +
                "      \"FSTG, Specialty determined by MLO\":{\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"Test Concept\":{\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"OR, Operation performed\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        JsonElement jsonConfig = new JsonParser().parse(jsonString);

        List<String> expected = Arrays.asList("FSTG, Specialty determined by MLO", "OR, Operation performed");
        List<String> ignoreConceptsSet = Arrays.asList("Test Concept", "Video");
        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        String ignoreConcepts = "Test Concept, Video";
        mockStatic(BatchUtils.class);
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(ignoreConceptsSet);
        when(BatchUtils.convertConceptNamesToSet(ignoreConcepts)).thenReturn(ignoreConceptsSet);
        whenNew(FileReader.class).withArguments("conf/app.json").thenThrow(new FileNotFoundException());
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertEquals(2, multiSelectAndAddMoreConceptNames.size());
        assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
        verifyStatic(times(1));
    }

    @Test
    public void shouldReturnListOfMultiSelectAndAddMoreDiscardingIgnoreConceptsEvenIfImplementationConfigFileIsMissing()
            throws Exception {
        String jsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"All Observation Templates\": {\n" +
                "        \"showPanelView\": false\n" +
                "      },\n" +
                "      \"Video\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      },\n" +
                "      \"FSTG, Specialty determined by MLO\":{\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"Test Concept\":{\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"OR, Operation performed\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonElement jsonConfig = new JsonParser().parse(jsonString);

        List<String> expected = Arrays.asList("FSTG, Specialty determined by MLO", "OR, Operation performed");
        List<String> ignoreConceptsSet = Arrays.asList("Test Concept", "Video");
        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        String ignoreConcepts = "Test Concept, Video";
        mockStatic(BatchUtils.class);
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(ignoreConceptsSet);
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenThrow(new FileNotFoundException());
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertEquals(2, multiSelectAndAddMoreConceptNames.size());
        assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
        verifyStatic(times(1));
    }

    @Test
    public void shouldReturnUniqueListOfMultiSelectAndAddMore() throws Exception {
        String defaultJsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"All Observation Templates\": {\n" +
                "        \"showPanelView\": false\n" +
                "      },\n" +
                "      \"Video\": {\n" +
                "        \"allowAddMore\": true,\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"Test Concept\":{\n" +
                "        \"multiSelect\":true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String implementationJsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"Demo Concept\": {\n" +
                "        \"xyz\": true\n" +
                "      },\n" +
                "      \"Test Concept\":{\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"OR, Operation performed\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        JsonElement defaultConfig = new JsonParser().parse(defaultJsonString);
        JsonElement implementationConfig = new JsonParser().parse(implementationJsonString);
        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(Arrays.asList());
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet(null)).thenReturn(Collections.emptyList());
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(implementationFileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(defaultConfig);
        when(jsonParser.parse(implementationFileReader)).thenReturn(implementationConfig);

        List<String> expected = Arrays.asList("OR, Operation performed", "Video", "Test Concept");

        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();

        assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
        verifyStatic(times(1));
    }

    @Test
    public void shouldReturnEmptyListForEmptyFile() throws Exception {
        String jsonString = "";
        JsonElement jsonConfig = new JsonParser().parse(jsonString);

        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        mockStatic(BatchUtils.class);
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(Arrays.asList("Test Concept, Video"));
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertTrue(multiSelectAndAddMoreConceptNames.isEmpty());
        verifyStatic(times(1));
    }

    @Test
    public void shouldReturnEmptyListForMissingConfigKey() throws Exception {
        String jsonString = "{\n" +
                "  \"missingConfig\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"All Observation Templates\": {\n" +
                "        \"showPanelView\": false\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonElement jsonConfig = new JsonParser().parse(jsonString);

        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        mockStatic(BatchUtils.class);
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(Arrays.asList());
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertTrue(multiSelectAndAddMoreConceptNames.isEmpty());
        verifyStatic(times(1));
    }

    @Test
    public void shouldReturnEmptyListForMissingConceptSetUIKey() throws Exception {
        String jsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"missingConceptSetUI\": {\n" +
                "      \"All Observation Templates\": {\n" +
                "        \"showPanelView\": false\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonElement jsonConfig = new JsonParser().parse(jsonString);

        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        mockStatic(BatchUtils.class);
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(Arrays.asList());
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertTrue(multiSelectAndAddMoreConceptNames.isEmpty());
        verifyStatic(times(1));
    }

    @Test
    public void shouldReturnListOfSeperateTableNames() throws Exception {

        String defaultJsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"All Observation Templates\": {\n" +
                "        \"showPanelView\": false\n" +
                "      },\n" +
                "      \"Video\": {\n" +
                "        \"allowAddMore\": true,\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"Test Concept\":{\n" +
                "        \"multiSelect\":true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String implementationJsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"Demo Concept\": {\n" +
                "        \"xyz\": true\n" +
                "      },\n" +
                "      \"Test Concept\":{\n" +
                "        \"multiSelect\":true\n" +
                "      },\n" +
                "      \"OR, Operation performed\": {\n" +
                "        \"allowAddMore\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        JsonElement defaultConfig = new JsonParser().parse(defaultJsonString);
        JsonElement implementationConfig = new JsonParser().parse(implementationJsonString);
        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        when(getIgnoreConceptNamesForObsJob(any())).thenReturn(Arrays.asList());
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet(null)).thenReturn(Collections.emptyList());
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(implementationFileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(defaultConfig);
        when(jsonParser.parse(implementationFileReader)).thenReturn(implementationConfig);

        mockStatic(JobDefinitionUtil.class);
        when(JobDefinitionUtil.getIgnoreConceptNamesForObsJob(any())).thenReturn(Arrays.asList(""));
        List<String> seprateTables = new ArrayList<>();
        seprateTables.add("separate table");
        when(JobDefinitionUtil.getSeparateTableNamesForObsJob(any())).thenReturn(seprateTables);
        List<String> expectedSeparateTables = Arrays.asList("OR, Operation performed", "Video", "Test Concept",
                "separate table");

        List<String> allSeparateConceptNames = separateTableConfigHelper
                .getAllSeparateTableConceptNames();

        assertEquals(4, allSeparateConceptNames.size());
        assertThat(expectedSeparateTables, containsInAnyOrder(allSeparateConceptNames.toArray()));

    }
}