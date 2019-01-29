package org.bahmni.mart.job;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.Form2StepConfigurer;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Form2ObsJobStrategy extends StepRegister implements JobStrategy  {

    private final Form2StepConfigurer form2StepConfigurer;

    @Autowired
    public Form2ObsJobStrategy(Form2StepConfigurer formStepConfigurer) {
        this.form2StepConfigurer = formStepConfigurer;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return getJob(form2StepConfigurer, jobDefinition);
    }
}
