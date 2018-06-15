package org.bahmni.mart.config.group;

import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.config.job.JobDefinitionUtil.setCommonPropertiesToGroupedJobs;
import static org.bahmni.mart.config.job.JobDefinitionUtil.setConfigToGroupedJobs;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobDefinitionUtil.class)
public class GroupedJobTest {

    GroupedJob groupedJob;
    @Mock
    private JobDefinition jobDefinition;
    @Mock
    private MartJSONReader martJSONReader;
    @Mock
    private ResourceLoader resourceLoader;

    @Before
    public void setUp() throws Exception {
        groupedJob = new GroupedJob();
        setValuesForMemberFields(groupedJob, "martJSONReader", martJSONReader);
        setValuesForMemberFields(groupedJob, "resourceLoader", resourceLoader);
    }

    @Test
    public void shouldReturnListOfGroupedJobs() {
        List<JobDefinition> expectedJobDefinitions = Arrays.asList(new JobDefinition(), new JobDefinition());

        mockStatic(JobDefinitionUtil.class);
        String programs = "programs";

        when(jobDefinition.getType()).thenReturn(programs);
        when(martJSONReader.getJobDefinitions(any(Resource.class)))
                .thenReturn(expectedJobDefinitions);

        List<JobDefinition> groupedJobDefinitions = groupedJob.getJobDefinitions(jobDefinition);

        verifyStatic(times(1));
        setCommonPropertiesToGroupedJobs(jobDefinition, expectedJobDefinitions);
        verifyStatic(times(1));
        setConfigToGroupedJobs(jobDefinition, expectedJobDefinitions);

        verify(martJSONReader, times(1)).getJobDefinitions(any(Resource.class));
        verify(jobDefinition, times(1)).getType();
        verify(resourceLoader, times(1)).getResource("classpath:groupedModules/" + programs + ".json");

        assertEquals(2, groupedJobDefinitions.size());

        containsInAnyOrder(expectedJobDefinitions, groupedJobDefinitions);
    }
}
