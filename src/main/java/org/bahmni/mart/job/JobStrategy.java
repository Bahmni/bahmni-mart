package org.bahmni.mart.job;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.springframework.batch.core.Job;

public interface JobStrategy {

    Job getJob(JobDefinition jobDefinition);
}
