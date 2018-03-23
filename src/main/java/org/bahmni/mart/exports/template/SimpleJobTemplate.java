package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.table.listener.TableGeneratorJobListener;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SimpleJobTemplate extends JobTemplate {

    @Autowired
    private TableGeneratorJobListener tableGeneratorJobListener;

    public Job buildJob(JobDefinition jobConfiguration) {
        return buildJob(jobConfiguration, tableGeneratorJobListener, jobConfiguration.getReaderSql());
    }
}
