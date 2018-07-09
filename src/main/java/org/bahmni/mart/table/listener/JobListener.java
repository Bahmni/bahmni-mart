package org.bahmni.mart.table.listener;

import org.bahmni.mart.helper.incrementalupdate.IncrementalUpdater;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.springframework.batch.core.BatchStatus.COMPLETED;

@Component
public class JobListener implements JobExecutionListener {

    @Autowired
    private IncrementalUpdater incrementalUpdater;

    @Override
    public void beforeJob(JobExecution jobExecution) {
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() != COMPLETED) {
            return;
        }
        String jobName = jobExecution.getJobInstance().getJobName();
        incrementalUpdater.updateMarker(jobName);
    }
}
