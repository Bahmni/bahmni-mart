package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.DiagnosesStepConfigurer;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiagnosesJobStrategy extends StepRegister implements JobStrategy {

    private final DiagnosesStepConfigurer diagnosesStepConfigurer;

    @Autowired
    public DiagnosesJobStrategy(DiagnosesStepConfigurer diagnosesStepConfigurer) {
        this.diagnosesStepConfigurer = diagnosesStepConfigurer;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return getJob(diagnosesStepConfigurer, jobDefinition);
    }
}
