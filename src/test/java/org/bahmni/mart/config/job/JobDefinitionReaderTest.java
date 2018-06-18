package org.bahmni.mart.config.job;

import org.bahmni.mart.BatchUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@PrepareForTest(BatchUtils.class)
@RunWith(PowerMockRunner.class)
public class JobDefinitionReaderTest {

    private JobDefinitionReader jobDefinitionReader;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(BatchUtils.class);
        String json = "{\"jobs\": [\n" +
                "  {\n" +
                "    \"name\": \"Program Data\",\n" +
                "    \"type\": \"customSql\",\n" +
                "    \"readerSql\": \"select * from program\",\n" +
                "    \"chunkSizeToRead\": \"1000\",\n" +
                "    \"tableName\": \"MyProgram\"\n" +
                "  },\n" +
                "{\n" +
                "  \"name\": \"Obs Data\",\n" +
                "  \"type\": \"obs\",\n" +
                "  \"conceptReferenceSource\": \"MSF-INTERNAL\",\n" +
                "  \"separateTableConfig\": {\n" +
                "    \"enableForAddMoreAndMultiSelect\": false,\n" +
                "    \"separateTables\": [\n" +
                "      \"FSTG, Specialty determined by MLO\",\n" +
                "      \"Stage\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"columnsToIgnore\": [\n" +
                "    \"MH, Name of MLO\"\n" +
                "  ]\n" +
                "}," +
                "{\n" +
                "      \"name\": \"programs\",\n" +
                "      \"type\": \"programs\",\n" +
                "      \"chunkSizeToRead\": 500" +
                "}" +
                "]}";
        when(BatchUtils.convertResourceOutputToString(any())).thenReturn(json);

        jobDefinitionReader = new JobDefinitionReader();
        jobDefinitionReader.read();
    }

    @Test
    public void shouldCreateAndReturnListOfJobDefinitions() {
        List<JobDefinition> jobDefinitions = jobDefinitionReader.getJobDefinitions();

        assertNotNull(jobDefinitions);
        assertEquals(3, jobDefinitions.size());
        JobDefinition genericJobDefinition = jobDefinitions.get(0);
        assertEquals("Program Data", genericJobDefinition.getName());
        assertEquals("customSql", genericJobDefinition.getType());
        assertEquals("select * from program", genericJobDefinition.getReaderSql());
        assertEquals(1000, genericJobDefinition.getChunkSizeToRead());
        assertEquals("MyProgram", genericJobDefinition.getTableName());

        JobDefinition obsJobDefinition = jobDefinitions.get(1);
        assertEquals("Obs Data", obsJobDefinition.getName());
        assertEquals("obs", obsJobDefinition.getType());

        List<String> expectedSeparateTables = Arrays.asList("FSTG, Specialty determined by MLO", "Stage");
        SeparateTableConfig separateTableConfig = obsJobDefinition.getSeparateTableConfig();
        assertNotNull(separateTableConfig);
        List<String> actualSeparateTables = separateTableConfig.getSeparateTables();
        assertEquals(2, actualSeparateTables.size());
        assertThat(expectedSeparateTables, containsInAnyOrder(actualSeparateTables.toArray()));

        List<String> expectedIgnoredColumns = Arrays.asList("MH, Name of MLO");
        List<String> actualColumnsToIgnore = obsJobDefinition.getColumnsToIgnore();
        assertEquals(1, actualColumnsToIgnore.size());
        assertThat(expectedIgnoredColumns, containsInAnyOrder(actualColumnsToIgnore.toArray()));

        JobDefinition groupedJobDefinition = jobDefinitions.get(2);
        assertEquals("programs", groupedJobDefinition.getName());
        assertEquals("programs", groupedJobDefinition.getType());
        assertEquals(500, groupedJobDefinition.getChunkSizeToRead());

    }

    @Test
    public void shouldReturnJobDefinitionGivenJobName() {
        JobDefinition programDataDefinition = jobDefinitionReader.getJobDefinitionByName("Program Data");

        assertEquals("Program Data", programDataDefinition.getName());
        assertEquals("customSql", programDataDefinition.getType());
        assertEquals("select * from program", programDataDefinition.getReaderSql());
        assertEquals(1000, programDataDefinition.getChunkSizeToRead());
        assertEquals("MyProgram", programDataDefinition.getTableName());
    }

    @Test
    public void shouldReturnEmptyJobDefinitionGivenInvalidName() {
        JobDefinition personAttributeDefinition = jobDefinitionReader.getJobDefinitionByName("InvalidName");

        assertNull(personAttributeDefinition.getName());
        assertNull(personAttributeDefinition.getType());
        assertNull(personAttributeDefinition.getReaderSql());
        assertEquals(0, personAttributeDefinition.getChunkSizeToRead());
        assertNull(personAttributeDefinition.getTableName());
    }

    @Test
    public void shouldReturnJobDefinitionsByGroupedJobType() {

        List<JobDefinition> jobDefinitionsByGroupedJobTypes = jobDefinitionReader.getJobDefinitionsByGroupedJobTypes();

        assertEquals(1, jobDefinitionsByGroupedJobTypes.size());
    }
}