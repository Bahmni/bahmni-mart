package org.bahmni.mart.config.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class CSVUploader {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    public Job buildJob(JobDefinition jobDefinition) {
        return jobBuilderFactory.get(jobDefinition.getName())
                .flow(step(jobDefinition))
                .end().build();
    }

    private Step step(JobDefinition jobDefinition) {
        return stepBuilderFactory.get(jobDefinition.getName())
                .tasklet(getCSVUploaderTasklet(jobDefinition.getReaderFilePath()))
                .build();
    }

    public abstract Tasklet getCSVUploaderTasklet(String readerFilePath);
}
