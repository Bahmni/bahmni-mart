package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.RegStepConfigurer;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class REGJobStrategy extends StepRegister implements JobStrategy {

    private final RegStepConfigurer regStepConfigurer;

    @Autowired
    public REGJobStrategy(RegStepConfigurer rspStepConfigurer) {
        this.regStepConfigurer = rspStepConfigurer;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return super.getJob(regStepConfigurer, jobDefinition);
    }
}
