package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.RspStepConfigurer;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RSPJobStrategy extends StepRegister implements JobStrategy {

    private final RspStepConfigurer rspStepConfigurer;

    @Autowired
    public RSPJobStrategy(RspStepConfigurer rspStepConfigurer) {
        this.rspStepConfigurer = rspStepConfigurer;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return super.getJob(rspStepConfigurer, jobDefinition);
    }
}
