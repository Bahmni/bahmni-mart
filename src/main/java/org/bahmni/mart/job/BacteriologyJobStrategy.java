package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.BacteriologyStepConfigurer;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BacteriologyJobStrategy extends StepRegister implements JobStrategy {

    private final BacteriologyStepConfigurer bacteriologyStepConfigurer;

    @Autowired
    public BacteriologyJobStrategy(BacteriologyStepConfigurer bacteriologyStepConfigurer) {
        this.bacteriologyStepConfigurer = bacteriologyStepConfigurer;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return getJob(bacteriologyStepConfigurer, jobDefinition);
    }
}
