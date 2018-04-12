package org.bahmni.mart.config.job;

import org.bahmni.mart.config.MartJSONReader;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class JobDefinitionReader extends MartJSONReader {

    @Override
    public List<JobDefinition> getJobDefinitions() {
        return super.getJobDefinitions();
    }

    public JobDefinition getJobDefinitionByName(String jobName) {
        Optional<JobDefinition> optionalJobDefinition = getJobDefinitions().stream()
                .filter(tempJobDefinition -> tempJobDefinition.getName().equals(jobName)).findFirst();
        return optionalJobDefinition.orElseGet(JobDefinition::new);
    }
}