package org.bahmni.mart.executors;

import org.bahmni.mart.config.group.GroupedJob;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionValidator;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.helper.MarkerManager;
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
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.springframework.batch.core.BatchStatus.COMPLETED;
import static org.springframework.batch.core.BatchStatus.FAILED;

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

    @Mock
    private MarkerManager markerManager;

    private JobDefinition jobDefinition = new JobDefinition();

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
        setValuesForMemberFields(martJobExecutor, "markerManager", markerManager);

        mockStatic(JobDefinitionValidator.class);

        when(jobDefinitionReader.getJobDefinitions()).thenReturn(singletonList(jobDefinition));
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

        verify(markerManager).insertMarkers(anyListOf(JobDefinition.class));
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
        verify(markerManager).insertMarkers(anyListOf(JobDefinition.class));
    }

    @Test
    public void shouldLogWarningWhenAnExceptionComesWhileLaunchingJobs() throws Exception {

        Logger log = mock(Logger.class);
        setValueForFinalStaticField(MartJobExecutor.class, "log", log);
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenThrow(new RuntimeException("some message"));

        martJobExecutor.execute();

        verify(log, times(2)).warn(any(String.class), any(Exception.class));

    }

    @Test
    public void shouldGiveListOfFailedJobs() throws Exception {
        JobExecution jobExecutionOfJob = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);
        when(jobLauncher.run(eq(job), any(JobParameters.class))).thenReturn(jobExecutionOfJob);
        when(jobExecutionOfJob.getStatus()).thenReturn(COMPLETED);
        when(jobExecutionOfJob.getJobInstance()).thenReturn(jobInstance);
        when(jobInstance.getJobName()).thenReturn("Completed Obs");

        JobExecution jobExecutionOfGroupJob = mock(JobExecution.class);
        JobInstance groupJobInstance = mock(JobInstance.class);
        when(jobLauncher.run(eq(groupJob), any(JobParameters.class))).thenReturn(jobExecutionOfGroupJob);
        when(jobExecutionOfGroupJob.getStatus()).thenReturn(FAILED);
        when(jobExecutionOfGroupJob.getJobInstance()).thenReturn(groupJobInstance);
        when(groupJobInstance.getJobName()).thenReturn("Failed Obs");

        martJobExecutor.execute();

        verify(jobLauncher).run(eq(job), any(JobParameters.class));
        verify(jobLauncher).run(eq(groupJob), any(JobParameters.class));
        verify(jobExecutionOfJob).getStatus();
        verify(jobExecutionOfJob, never()).getJobInstance();
        verify(jobInstance, never()).getJobName();
        verify(jobExecutionOfGroupJob).getStatus();
        verify(jobExecutionOfGroupJob).getJobInstance();
        verify(groupJobInstance).getJobName();
        verify(jobExecutionOfGroupJob.getJobInstance()).getJobName();

        assertTrue(singletonList("Failed Obs").containsAll(martJobExecutor.getFailedJobs()));
    }
}