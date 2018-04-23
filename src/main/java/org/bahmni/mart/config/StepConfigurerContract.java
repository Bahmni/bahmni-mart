package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinition;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;

public interface StepConfigurerContract {

    void createTables();

    void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport, JobDefinition jobDefinition);

}
