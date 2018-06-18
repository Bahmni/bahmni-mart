package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.StepConfigurerContract;
import org.bahmni.mart.exports.TreatmentRegistrationBaseExportStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class StepRegister {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    Job getJob(StepConfigurerContract stepConfigurerContract, JobDefinition jobDefinition) {
        FlowBuilder<FlowJobBuilder> completeDataExport = getFlowBuilder(jobDefinition.getName());
        stepConfigurerContract.registerSteps(completeDataExport, jobDefinition);
        stepConfigurerContract.createTables();

        return completeDataExport.end().build();
    }

    private FlowBuilder<FlowJobBuilder> getFlowBuilder(String jobName) {
        //TODO: Have to remove treatmentRegistrationBaseExportStep from flow
        return jobBuilderFactory.get(jobName)
                .incrementer(new RunIdIncrementer()).preventRestart()
                .flow(treatmentRegistrationBaseExportStep.getStep());
    }
}
