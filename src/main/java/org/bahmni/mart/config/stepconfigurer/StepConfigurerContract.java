package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;

public interface StepConfigurerContract {

    void generateTableData(JobDefinition jobDefinition);

    void createTables(JobDefinition jobDefinition);

    void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport, JobDefinition jobDefinition);

}
