package org.bahmni.mart.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
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
import java.util.HashSet;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getSeparateTableNamesForJob;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({SeparateTableConfigHelper.class, JsonParser.class, JobDefinitionUtil.class})
@RunWith(PowerMockRunner.class)
public class SeparateTableConfigHelperTest {

    @Mock
    private FileReader fileReader;

    @Mock
    private FileReader implementationFileReader;

    @Mock
    private JsonParser jsonParser;

    private SeparateTableConfigHelper separateTableConfigHelper;


    @Before
    public void setUp() throws Exception {
        separateTableConfigHelper = new SeparateTableConfigHelper();
        mockStatic(JobDefinitionUtil.class);
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


        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);

        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();

        assertEquals(3, multiSelectAndAddMoreConceptNames.size());
        assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
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

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);

        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertTrue(multiSelectAndAddMoreConceptNames.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListWhenFileIsNotPresentInTheGivenPath() throws Exception {
        whenNew(FileReader.class).withArguments("test1.json").thenThrow(new FileNotFoundException());
        whenNew(FileReader.class).withArguments("test2.json").thenThrow(new FileNotFoundException());
        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "test1.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "test2.json");

        assertTrue(separateTableConfigHelper.getAddMoreAndMultiSelectConceptNames().isEmpty());
    }

    @Test
    public void shouldReturnListOfMultiSelectAndAddMoreConceptsEvenIfDefaultConfigFileIsMissing()
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

        List<String> expected = Arrays.asList("FSTG, Specialty determined by MLO", "OR, Operation performed",
                "Video", "Test Concept");
        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");

        whenNew(FileReader.class).withArguments("conf/app.json").thenThrow(new FileNotFoundException());
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);

        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();

        assertEquals(4, multiSelectAndAddMoreConceptNames.size());
        assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void shouldReturnListOfMultiSelectAndAddMoreConceptsEvenIfImplementationConfigFileIsMissing()
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

        List<String> expected = Arrays.asList("FSTG, Specialty determined by MLO", "OR, Operation performed",
                "Test Concept", "Video");
        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenThrow(new FileNotFoundException());
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);

        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();

        assertEquals(4, multiSelectAndAddMoreConceptNames.size());
        assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
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


        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(implementationFileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(defaultConfig);
        when(jsonParser.parse(implementationFileReader)).thenReturn(implementationConfig);

        List<String> expected = Arrays.asList("OR, Operation performed", "Video", "Test Concept");

        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();

        assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void shouldReturnEmptyListForEmptyFile() throws Exception {
        String jsonString = "";
        JsonElement jsonConfig = new JsonParser().parse(jsonString);

        setValuesForMemberFields(separateTableConfigHelper, "defaultConfigFile", "conf/app.json");
        setValuesForMemberFields(separateTableConfigHelper, "implementationConfigFile", "conf/random/app.json");
        
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertTrue(multiSelectAndAddMoreConceptNames.isEmpty());
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
        
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);
        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertTrue(multiSelectAndAddMoreConceptNames.isEmpty());
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
        
        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonConfig);

        List<String> multiSelectAndAddMoreConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();
        assertTrue(multiSelectAndAddMoreConceptNames.isEmpty());
    }

    @Test
    public void shouldGiveMorePriorityToImplementationConfigIfDifferentConfigIsPresentInBothFiles() throws Exception {
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
                "        \"multiSelect\":false\n" +
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

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(implementationFileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(defaultConfig);
        when(jsonParser.parse(implementationFileReader)).thenReturn(implementationConfig);

        when(getSeparateTableNamesForJob(any()))
                .thenReturn(Collections.singletonList("separate table"));
        List<String> expectedSeparateTables = Arrays.asList("OR, Operation performed", "Video");

        List<String> allSeparateConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();

        assertEquals(2, allSeparateConceptNames.size());
        assertThat(expectedSeparateTables, containsInAnyOrder(allSeparateConceptNames.toArray()));
    }

    @Test
    public void shouldReturnListOfSeparateTableNamesWhenAddMoreMultiSelectFlagIsTrueInJobDefinition() throws Exception {

        JobDefinition jobDefinition = mock(JobDefinition.class);
        mockStatic(JobDefinitionUtil.class);
        List<String> separateTableNames = new ArrayList<>();
        separateTableNames.add("obs separate table");

        when(JobDefinitionUtil.getSeparateTableNamesForJob(jobDefinition)).thenReturn(separateTableNames);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(true);

        Concept concept1 = new Concept(1, "OR, Operation performed", 0);
        Concept concept2 = new Concept(2, "Video", 0);
        Concept concept3 = new Concept(3, "Test Concept", 0);
        Concept concept4 = new Concept(4, "obs separate table", 0);

        ConceptService conceptService = mock(ConceptService.class);
        setValuesForMemberFields(separateTableConfigHelper, "conceptService", conceptService);
        when(conceptService.getConceptsByNames(separateTableNames))
                .thenReturn(Arrays.asList(concept1, concept2, concept3, concept4));

        setValuesForMemberFields(separateTableConfigHelper, "defaultAddMoreAndMultiSelectConceptsNames",
                Arrays.asList("OR, Operation performed", "Video", "Test Concept"));

        HashSet<Concept> expectedSeparateTables = new HashSet<>(Arrays.asList(concept1, concept2, concept3, concept4));

        HashSet<Concept> allSeparateConceptNames = separateTableConfigHelper
                .getSeparateTableConceptsForJob(jobDefinition);

        assertEquals(4, allSeparateConceptNames.size());
        assertThat(expectedSeparateTables, containsInAnyOrder(allSeparateConceptNames.toArray()));

        verify(JobDefinitionUtil.class, times(1));
        JobDefinitionUtil.getSeparateTableNamesForJob(jobDefinition);

        verify(conceptService, times(1)).getConceptsByNames(any());

    }

    @Test
    public void shouldReturnListOfSeparateTablesWhenAddMoreMultiSelectFlagIsFalseInJobDefinition() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        mockStatic(JobDefinitionUtil.class);
        List<String> separateTableNames = new ArrayList<>();
        separateTableNames.add("obs separate table");

        when(JobDefinitionUtil.getSeparateTableNamesForJob(jobDefinition)).thenReturn(separateTableNames);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(false);

        Concept concept1 = new Concept(1, "OR, Operation performed", 0);

        ConceptService conceptService = mock(ConceptService.class);
        setValuesForMemberFields(separateTableConfigHelper, "conceptService", conceptService);
        when(conceptService.getConceptsByNames(separateTableNames)).thenReturn(Collections.singletonList(concept1));

        setValuesForMemberFields(separateTableConfigHelper, "defaultAddMoreAndMultiSelectConceptsNames",
                Arrays.asList("OR, Operation performed", "Video", "Test Concept"));

        HashSet<Concept> expectedSeparateTables = new HashSet<>(Collections.singletonList(concept1));

        HashSet<Concept> allSeparateConceptNames = separateTableConfigHelper
                .getSeparateTableConceptsForJob(jobDefinition);

        assertEquals(1, allSeparateConceptNames.size());
        assertThat(expectedSeparateTables, containsInAnyOrder(allSeparateConceptNames.toArray()));

        verify(JobDefinitionUtil.class, times(1));
        JobDefinitionUtil.getSeparateTableNamesForJob(jobDefinition);

        verify(JobDefinitionUtil.class, times(1));
        JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition);

        verify(conceptService, times(1)).getConceptsByNames(any());
    }

    @Test
    public void shouldReturnEmptyHashSetWhenThereAreNoSeparateTablesAndDefaultAddMoreAndMultiSelects()
            throws Exception {

        JobDefinition jobDefinition = mock(JobDefinition.class);
        mockStatic(JobDefinitionUtil.class);

        List<String> separateTableNames = new ArrayList<>();
        when(JobDefinitionUtil.getSeparateTableNamesForJob(jobDefinition))
                .thenReturn(separateTableNames);

        ConceptService conceptService = mock(ConceptService.class);
        setValuesForMemberFields(separateTableConfigHelper, "conceptService", conceptService);
        when(conceptService.getConceptsByNames(separateTableNames))
                .thenReturn(Arrays.asList());

        setValuesForMemberFields(separateTableConfigHelper, "defaultAddMoreAndMultiSelectConceptsNames",
                Arrays.asList());

        HashSet<Concept> allSeparateConceptNames = separateTableConfigHelper
                .getSeparateTableConceptsForJob(jobDefinition);

        assertEquals(0, allSeparateConceptNames.size());

        verify(JobDefinitionUtil.class, times(1));
        JobDefinitionUtil.getSeparateTableNamesForJob(jobDefinition);
    }

    @Test
    public void shouldMergeBothConfigsAndGivePriorityToImplementationConfig() throws Exception {
        String defaultJsonString = "{\n" +
                "  \"config\": {\n" +
                "    \"conceptSetUI\": {\n" +
                "      \"All Observation Templates\": {\n" +
                "        \"showPanelView\": false\n" +
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
                "        \"multiSelect\":false\n" +
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


        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(implementationFileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(defaultConfig);
        when(jsonParser.parse(implementationFileReader)).thenReturn(implementationConfig);

        when(getSeparateTableNamesForJob(any()))
                .thenReturn(Collections.singletonList("separate table"));
        List<String> expectedSeparateTables = Collections.singletonList("OR, Operation performed");

        List<String> allSeparateConceptNames = separateTableConfigHelper
                .getAddMoreAndMultiSelectConceptNames();

        assertEquals(1, allSeparateConceptNames.size());
        assertThat(expectedSeparateTables, containsInAnyOrder(allSeparateConceptNames.toArray()));
    }
}