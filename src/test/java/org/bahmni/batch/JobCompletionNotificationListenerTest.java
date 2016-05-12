package org.bahmni.batch;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = Application.class)
public class JobCompletionNotificationListenerTest {

        @Autowired
        JobCompletionNotificationListener listener;

        @Test
        public void testAfterJob() throws Exception {

            final JobExecution jobExecution = mock(JobExecution.class);
            when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);
            listener.afterJob(jobExecution);
            verify(jobExecution).getStatus();
        }
    }
