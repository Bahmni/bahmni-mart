package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.exports.template.SimpleJobTemplate;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomSqlStrategy implements JobStrategy {

    private final ObjectFactory<SimpleJobTemplate> simpleJobTemplateFactory;

    @Autowired
    public CustomSqlStrategy(ObjectFactory<SimpleJobTemplate> simpleJobTemplateFactory) {
        this.simpleJobTemplateFactory = simpleJobTemplateFactory;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return simpleJobTemplateFactory.getObject().buildJob(jobDefinition);
    }
}
