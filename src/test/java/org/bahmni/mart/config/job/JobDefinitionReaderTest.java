package org.bahmni.mart.config.job;

import org.bahmni.mart.BatchUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(BatchUtils.class)
@RunWith(PowerMockRunner.class)
public class JobDefinitionReaderTest {

    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private Resource jobDefinition;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        jobDefinitionReader = new JobDefinitionReader();
        PowerMockito.mockStatic(BatchUtils.class);
        String json = "[\n" +
                "  {\n" +
                "    \"name\": \"Program Data\",\n" +
                "    \"type\": \"generic\",\n" +
                "    \"readerSql\": \"select * from program\",\n" +
                "    \"chunkSizeToRead\": \"1000\",\n" +
                "    \"tableName\": \"MyProgram\"\n" +
                "  }\n" +
                "]";
        setValuesForMemberFields(jobDefinitionReader, "jobDefinition", jobDefinition);
        when(BatchUtils.convertResourceOutputToString(jobDefinition)).thenReturn(json);
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
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinition.getType()).thenReturn("obs");
        setValuesForMemberFields(jobDefinitionReader, "jobDefinitions", Arrays.asList(jobDefinition));

        String conceptReferenceSource = jobDefinitionReader.getConceptReferenceSource();

        assertEquals("", conceptReferenceSource);
    }

    @Test
    public void shouldReturnJobDefinitionGivenJobName() throws NoSuchFieldException, IllegalAccessException {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        setValuesForMemberFields(jobDefinitionReader,"jobDefinitions", Arrays.asList(jobDefinition));

        when(jobDefinition.getName()).thenReturn("Person Attributes");
        JobDefinition personAttributeDefinition = jobDefinitionReader.getJobDefinitionByName("Person Attributes");

        assertEquals(jobDefinition, personAttributeDefinition);
    }

    @Test
    public void shouldReturnEmptyJobDefinitionGivenInvalidName() throws NoSuchFieldException, IllegalAccessException {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        setValuesForMemberFields(jobDefinitionReader,"jobDefinitions", Arrays.asList(jobDefinition));

        when(jobDefinition.getName()).thenReturn("Person Attributes");
        JobDefinition personAttributeDefinition = jobDefinitionReader.getJobDefinitionByName("InvalidName");

        assertNotEquals(jobDefinition, personAttributeDefinition);
    }
}