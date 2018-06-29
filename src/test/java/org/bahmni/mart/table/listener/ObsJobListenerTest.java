package org.bahmni.mart.table.listener;

import org.bahmni.mart.CommonTestHelper;
import org.bahmni.mart.helper.IncrementalUpdater;
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
public class ObsJobListenerTest {

    @Mock
    private JobExecution jobExecution;

    @Mock
    private JobInstance jobInstance;

    @Mock
    private IncrementalUpdater incrementalUpdater;

    private ObsJobListener obsJobListener;

    @Before
    public void setUp() throws Exception {
        obsJobListener = new ObsJobListener();
        CommonTestHelper.setValuesForMemberFields(obsJobListener, "incrementalUpdater", incrementalUpdater);
    }

    @Test
    public void shouldUpdateMarkerOnSuccessfulJobExecution() {
        String jobName = "obs";
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(jobInstance.getJobName()).thenReturn(jobName);

        obsJobListener.afterJob(jobExecution);

        verify(jobExecution).getJobInstance();
        verify(jobInstance).getJobName();
        verify(incrementalUpdater).updateMarker(jobName);
    }

    @Test
    public void shouldNotUpdateMarkerWhenJobExecutionIsNotCompleted() {
        when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);

        obsJobListener.afterJob(jobExecution);

        verify(incrementalUpdater, never()).updateMarker(anyString());
    }
}