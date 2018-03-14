package org.bahmni.mart.config.job;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.exports.SimpleJobTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Job;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@PrepareForTest(BatchUtils.class)
@RunWith(PowerMockRunner.class)
public class JobDefinitionReaderTest {

    private JobDefinitionReader jobDefinitionReader;

    private String json;

    @Mock
    private Resource jobDefinion;

    @Mock
    private SimpleJobTemplate simpleJobTemplate;


    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        jobDefinitionReader = new JobDefinitionReader();
        PowerMockito.mockStatic(BatchUtils.class);
        json = "[\n" +
                "  {\n" +
                "    \"name\": \"Program Data\",\n" +
                "    \"type\": \"program\",\n" +
                "    \"readerSql\": \"select * from program\",\n" +
                "    \"chunkSizeToRead\": \"1000\",\n" +
                "    \"tableName\": \"MyProgram\"\n" +
                "  }\n" +
                "]";
        setValuesForMemberFields(jobDefinitionReader, "jobDefinition", jobDefinion);
        setValuesForMemberFields(jobDefinitionReader, "simpleJobTemplate", simpleJobTemplate);
        when(BatchUtils.convertResourceOutputToString(jobDefinion)).thenReturn(json);
    }

    @Test
    public void shouldCreateAndReturnListOfJobDefinitions() {
        List<JobDefinition> jobDefinitions = jobDefinitionReader.getJobDefinitions();

        assertNotNull(jobDefinitions);
        assertEquals(1, jobDefinitions.size());
        assertEquals("Program Data", jobDefinitions.get(0).getName());
        assertEquals("program", jobDefinitions.get(0).getType());
        assertEquals("select * from program", jobDefinitions.get(0).getReaderSql());
        assertEquals(1000, jobDefinitions.get(0).getChunkSizeToRead());
        assertEquals("MyProgram", jobDefinitions.get(0).getTableName());
    }

    @Test
    public void shouldReturnListOfJobs() {
        List<JobDefinition> jobDefinitions = jobDefinitionReader.getJobDefinitions();
        Job expectedJob = mock(Job.class);
        when(simpleJobTemplate.buildJob(jobDefinitions.get(0))).thenReturn(expectedJob);

        List<Job> jobs = jobDefinitionReader.jobs();

        verify(simpleJobTemplate, times(1)).buildJob(jobDefinitions.get(0));
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals(expectedJob, jobs.get(0));
    }
}