package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinition;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;

public interface StepConfigurerContract {

    void generateTableData(JobDefinition jobDefinition);

    void createTables();

    void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport, JobDefinition jobDefinition);

}
