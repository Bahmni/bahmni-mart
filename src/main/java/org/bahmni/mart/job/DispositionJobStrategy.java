package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.DispositionStepConfigurer;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DispositionJobStrategy extends StepRegister implements JobStrategy {

    private final DispositionStepConfigurer dispositionStepConfigurer;

    @Autowired
    public DispositionJobStrategy(DispositionStepConfigurer dispositionStepConfigurer) {
        this.dispositionStepConfigurer = dispositionStepConfigurer;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return getJob(dispositionStepConfigurer, jobDefinition);
    }
}
