package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.OrderStepConfigurer;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderJobStrategy extends StepRegister implements JobStrategy {

    private final OrderStepConfigurer orderStepConfigurer;

    @Autowired
    public OrderJobStrategy(OrderStepConfigurer orderStepConfigurer) {
        this.orderStepConfigurer = orderStepConfigurer;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return getJob(orderStepConfigurer, jobDefinition);
    }
}
