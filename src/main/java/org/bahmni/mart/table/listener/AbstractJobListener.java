package org.bahmni.mart.table.listener;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

public abstract class AbstractJobListener extends JobExecutionListenerSupport {
    @Autowired
    protected JobDefinitionReader jobDefinitionReader;

    @Autowired
    @Qualifier("openmrsJdbcTemplate")
    protected JdbcTemplate openMRSJdbcTemplate;

    @Autowired
    protected TableGeneratorStep tableGeneratorStep;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        try {
            createTable(jobExecution.getJobInstance().getJobName());
        } catch (Exception e) {
            logError(e);
            jobExecution.stop();
        }
    }

    private void createTable(String jobName) {
        tableGeneratorStep.createTables(Arrays.asList(getTableDataForMart(jobName)));
    }

    public abstract TableData getTableDataForMart(String jobName);

    abstract void logError(Exception e);
}
