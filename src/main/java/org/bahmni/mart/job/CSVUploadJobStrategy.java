package org.bahmni.mart.job;

import org.bahmni.mart.config.job.CustomCodesUploader;
import org.bahmni.mart.config.job.JobDefinition;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CSVUploadJobStrategy implements JobStrategy {

    private final CustomCodesUploader customCodesUploader;

    @Autowired
    public CSVUploadJobStrategy(CustomCodesUploader customCodesUploader) {
        this.customCodesUploader = customCodesUploader;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return customCodesUploader.buildJob(jobDefinition);
    }
}
