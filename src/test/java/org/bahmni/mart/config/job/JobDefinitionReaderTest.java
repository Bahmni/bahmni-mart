package org.bahmni.mart.config.job;

import org.bahmni.mart.BatchUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(BatchUtils.class)
@RunWith(PowerMockRunner.class)
public class JobDefinitionReaderTest {

    private JobDefinitionReader jobDefinitionReader;

    private String json;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        PowerMockito.mockStatic(BatchUtils.class);
        json = "{\"jobs\": [\n" +
                "  {\n" +
                "    \"name\": \"Program Data\",\n" +
                "    \"type\": \"generic\",\n" +
                "    \"readerSql\": \"select * from program\",\n" +
                "    \"chunkSizeToRead\": \"1000\",\n" +
                "    \"tableName\": \"MyProgram\"\n" +
                "  }\n" +
                "]}";
        when(BatchUtils.convertResourceOutputToString(any())).thenReturn(json);

        jobDefinitionReader = new JobDefinitionReader();
        jobDefinitionReader.read();
    }

    @Test
    public void shouldCreateAndReturnListOfJobDefinitions() {
        List<JobDefinition> jobDefinitions = jobDefinitionReader.getJobDefinitions();

        assertNotNull(jobDefinitions);
        assertEquals(1, jobDefinitions.size());
        assertEquals("Program Data", jobDefinitions.get(0).getName());
        assertEquals("generic", jobDefinitions.get(0).getType());
        assertEquals("select * from program", jobDefinitions.get(0).getReaderSql());
        assertEquals(1000, jobDefinitions.get(0).getChunkSizeToRead());
        assertEquals("MyProgram", jobDefinitions.get(0).getTableName());
    }

    @Test
    @Ignore
    public void shouldReturnConceptReferenceSourceIfItIsPresent() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinition.getType()).thenReturn("obs");
        when(jobDefinition.getConceptReferenceSource()).thenReturn("BAHMNI_INTERNAL");
        setValuesForMemberFields(jobDefinitionReader, "jobDefinitions", Arrays.asList(jobDefinition));

        String conceptReferenceSource = jobDefinitionReader.getConceptReferenceSource();

        assertEquals("BAHMNI_INTERNAL", conceptReferenceSource);

    }

    @Test
    public void shouldReturnEmptyStringIfConceptReferenceSourceIsNotPresent() throws Exception {

        String conceptReferenceSource = jobDefinitionReader.getConceptReferenceSource();

        assertEquals("", conceptReferenceSource);
    }

    @Test
    public void shouldReturnJobDefinitionGivenJobName() throws NoSuchFieldException, IllegalAccessException {
        JobDefinition programDataDefinition = jobDefinitionReader.getJobDefinitionByName("Program Data");

        assertEquals("Program Data", programDataDefinition.getName());
        assertEquals("generic", programDataDefinition.getType());
        assertEquals("select * from program", programDataDefinition.getReaderSql());
        assertEquals(1000, programDataDefinition.getChunkSizeToRead());
        assertEquals("MyProgram", programDataDefinition.getTableName());
    }

    @Test
    public void shouldReturnEmptyJobDefinitionGivenInvalidName() throws NoSuchFieldException, IllegalAccessException {
        JobDefinition personAttributeDefinition = jobDefinitionReader.getJobDefinitionByName("InvalidName");

        assertNull(personAttributeDefinition.getName());
        assertNull(personAttributeDefinition.getType());
        assertNull(personAttributeDefinition.getReaderSql());
        assertEquals(0, personAttributeDefinition.getChunkSizeToRead());
        assertNull(personAttributeDefinition.getTableName());
    }
}