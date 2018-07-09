package org.bahmni.mart.table.listener;

import org.bahmni.mart.CommonTestHelper;
import org.bahmni.mart.helper.incrementalupdate.IncrementalUpdater;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobListenerTest {

    @Mock
    private JobExecution jobExecution;

    @Mock
    private JobInstance jobInstance;

    @Mock
    private IncrementalUpdater incrementalUpdater;

    private JobListener jobListener;

    @Before
    public void setUp() throws Exception {
        jobListener = new JobListener();
        CommonTestHelper.setValuesForMemberFields(jobListener, "incrementalUpdater", incrementalUpdater);
    }

    @Test
    public void shouldUpdateMarkerOnSuccessfulJobExecution() {
        String jobName = "obs";
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(jobInstance.getJobName()).thenReturn(jobName);

        jobListener.afterJob(jobExecution);

        verify(jobExecution).getJobInstance();
        verify(jobInstance).getJobName();
        verify(incrementalUpdater).updateMarker(jobName);
    }

    @Test
    public void shouldNotUpdateMarkerWhenJobExecutionIsNotCompleted() {
        when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);

        jobListener.afterJob(jobExecution);

        verify(incrementalUpdater, never()).updateMarker(anyString());
    }
}