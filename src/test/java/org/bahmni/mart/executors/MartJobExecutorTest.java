package org.bahmni.mart.executors;

import org.bahmni.mart.config.group.GroupedJob;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionValidator;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.job.JobContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.Arrays;
import java.util.Collections;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest(JobDefinitionValidator.class)
@RunWith(PowerMockRunner.class)
public class MartJobExecutorTest {

    private MartJobExecutor martJobExecutor;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private JobContext jobContext;

    @Mock
    private GroupedJob groupedJob;

    @Mock
    private Job job;

    @Mock
    private Job groupJob;

    private JobDefinition jobDefinition = new JobDefinition();

    private JobDefinition groupedTypeJobDefinition = new JobDefinition();

    private JobDefinition groupedJobDefinition = new JobDefinition();

    @Rule
    private ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        martJobExecutor = new MartJobExecutor();

        setValuesForMemberFields(martJobExecutor, "jobDefinitionReader", jobDefinitionReader);
        setValuesForMemberFields(martJobExecutor, "jobLauncher", jobLauncher);
        setValuesForMemberFields(martJobExecutor, "jobContext", jobContext);
        setValuesForMemberFields(martJobExecutor, "groupedJob", groupedJob);

        mockStatic(JobDefinitionValidator.class);

        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Collections.singletonList(jobDefinition));
        when(groupedJob.getJobDefinitionsBySkippingGroupedTypeJobs(anyListOf(JobDefinition.class)))
                .thenReturn(Arrays.asList(jobDefinition, groupedJobDefinition, null));

        when(JobDefinitionValidator.validate(anyListOf(JobDefinition.class))).thenReturn(true);

        when(jobContext.getJob(jobDefinition)).thenReturn(job);
        when(jobContext.getJob(groupedJobDefinition)).thenReturn(groupJob);
    }

    @Test
    public void shouldExecuteAllTheJobs() throws Exception {

        martJobExecutor.execute();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(groupedJob, times(1)).getJobDefinitionsBySkippingGroupedTypeJobs(anyListOf(JobDefinition.class));

        verifyStatic(times(1));
        JobDefinitionValidator.validate(anyListOf(JobDefinition.class));

        verify(jobContext, times(1)).getJob(jobDefinition);
        verify(jobContext, times(1)).getJob(groupedJobDefinition);
        verify(job, times(1)).getName();
        verify(groupJob, times(1)).getName();
        verify(groupedJob, times(1)).getJobDefinitionsBySkippingGroupedTypeJobs(anyListOf(JobDefinition.class));
        verify(jobLauncher, times(2)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void shouldThrowInvalidJobConfigurationForInvalidJobs() {

        when(JobDefinitionValidator.validate(anyListOf(JobDefinition.class))).thenReturn(false);

        expectedException.expect(InvalidJobConfiguration.class);
        expectedException.expectMessage("Invalid Job Configuration");

        martJobExecutor.execute();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();

        verifyStatic(times(1));
        JobDefinitionValidator.validate(anyListOf(JobDefinition.class));
    }

    @Test
    public void shouldLogWarningWhenAnExceptionComesWhileLaunchingJobs() throws Exception {

        Logger log = mock(Logger.class);
        setValueForFinalStaticField(MartJobExecutor.class, "log", log);
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenThrow(new RuntimeException("some message"));

        martJobExecutor.execute();

        verify(log, times(2)).warn(any(String.class), any(Exception.class));

    }
}