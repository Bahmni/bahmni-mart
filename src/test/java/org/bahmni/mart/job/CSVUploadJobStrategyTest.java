package org.bahmni.mart.job;

import org.bahmni.mart.config.job.CustomCodesUploader;
import org.bahmni.mart.config.job.JobDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.Job;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CSVUploadJobStrategyTest {

    @Mock
    private CustomCodesUploader customCodesUploader;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private Job expectedJob;

    @Test
    public void shouldReturnAJob() {

        when(customCodesUploader.buildJob(jobDefinition)).thenReturn(expectedJob);

        CSVUploadJobStrategy csvUploadJobStrategy = new CSVUploadJobStrategy(customCodesUploader);

        Job actualJob = csvUploadJobStrategy.getJob(jobDefinition);

        assertEquals(expectedJob, actualJob);
    }
}