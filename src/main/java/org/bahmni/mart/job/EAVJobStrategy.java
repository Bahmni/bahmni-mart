package org.bahmni.mart.job;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.template.EAVJobTemplate;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EAVJobStrategy implements JobStrategy {

    private final ObjectFactory<EAVJobTemplate> eavJobTemplateFactory;

    @Autowired
    public EAVJobStrategy(ObjectFactory<EAVJobTemplate> eavJobTemplateFactory) {
        this.eavJobTemplateFactory = eavJobTemplateFactory;
    }

    @Override
    public Job getJob(JobDefinition jobDefinition) {
        return eavJobTemplateFactory.getObject().buildJob(jobDefinition);
    }
}
