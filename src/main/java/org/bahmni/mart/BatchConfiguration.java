package org.bahmni.mart;

import org.bahmni.mart.executors.MartExecutor;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer implements CommandLineRunner {

    @Autowired
    private List<MartExecutor> martExecutors;

    @Override
    public void run(String... args) {

        martExecutors.forEach(MartExecutor::execute);

    }
}