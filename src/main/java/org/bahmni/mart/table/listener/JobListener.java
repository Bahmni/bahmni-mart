package org.bahmni.mart.table.listener;

import org.bahmni.mart.exports.updatestrategy.SimpleIncrementalUpdateStrategy;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.springframework.batch.core.BatchStatus.COMPLETED;

@Component
public class JobListener extends JobExecutionListenerSupport {

    @Autowired
    private SimpleIncrementalUpdateStrategy incrementalUpdater;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (COMPLETED.equals(jobExecution.getStatus()))
            incrementalUpdater.updateMarker(jobExecution.getJobInstance().getJobName());
    }
}
