package org.bahmni.mart.config.job;

import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

import static org.bahmni.mart.table.FormTableMetadataGenerator.getProcessedName;

@Configuration
public class JobDefinitionReader extends MartJSONReader {

    @Override
    public List<JobDefinition> getJobDefinitions() {
        return super.getJobDefinitions();
    }

    //TODO: REFACTOR getJobDefinitionByName AND getJobDefinitionByProcessedName METHOD

    public JobDefinition getJobDefinitionByName(String jobName) {
        Optional<JobDefinition> optionalJobDefinition = getJobDefinitions().stream()
                .filter(tempJobDefinition -> tempJobDefinition.getName().equals(jobName)).findFirst();
        return optionalJobDefinition.orElseGet(JobDefinition::new);
    }

    public JobDefinition getJobDefinitionByProcessedName(String processedJobName) {
        Optional<JobDefinition> optionalJobDefinition = getJobDefinitions().stream()
                .filter(tempJobDefinition -> processedJobName.equals(getProcessedName(tempJobDefinition.getName())))
                .findFirst();
        return optionalJobDefinition.orElseGet(JobDefinition::new);
    }
}