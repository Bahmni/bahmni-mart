package org.bahmni.mart;

import org.bahmni.mart.executors.MartExecutor;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer implements CommandLineRunner {

    @Autowired
    private List<MartExecutor> martExecutors;

    @Value("${spring.batch.job.enabled:true}")
    private Boolean shouldRunBatchJob;

    public void setShouldRunBatchJob(Boolean shouldRunBatchJob) {
        this.shouldRunBatchJob = shouldRunBatchJob;
    }

    @Override
    public void run(String... args) {
        if (shouldRunBatchJob) {
            martExecutors.forEach(MartExecutor::execute);
        }
    }
}