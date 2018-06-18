package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.MetaDataStepConfigurer;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetaDataJobStrategy extends StepRegister implements JobStrategy {

    private final MetaDataStepConfigurer metaDataStepConfigurer;

    @Autowired
    public MetaDataJobStrategy(MetaDataStepConfigurer metaDataStepConfigurer) {
        this.metaDataStepConfigurer = metaDataStepConfigurer;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return getJob(metaDataStepConfigurer, jobDefinition);
    }
}
