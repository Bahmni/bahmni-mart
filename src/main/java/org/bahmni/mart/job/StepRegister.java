package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.StepConfigurerContract;
import org.bahmni.mart.exports.DummyStep;
import org.bahmni.mart.table.listener.JobListener;
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
    private JobListener jobListener;

    @Autowired
    private DummyStep dummyStep;

    Job getJob(StepConfigurerContract stepConfigurerContract, JobDefinition jobDefinition) {
        FlowBuilder<FlowJobBuilder> completeDataExport = getFlowBuilder(jobDefinition.getName());
        stepConfigurerContract.generateTableData(jobDefinition);
        stepConfigurerContract.createTables();
        stepConfigurerContract.registerSteps(completeDataExport, jobDefinition);

        return completeDataExport.end().build();
    }

    private FlowBuilder<FlowJobBuilder> getFlowBuilder(String jobName) {
        return jobBuilderFactory.get(jobName)
                .incrementer(new RunIdIncrementer()).preventRestart()
                .listener(jobListener)
                .flow(dummyStep.getStep());
    }
}
