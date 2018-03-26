package org.bahmni.mart.config.job;

import com.google.gson.Gson;
import org.bahmni.mart.BatchUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Configuration
public class JobDefinitionReader {

    @Value("${bahmniMartConfigFile}")
    private Resource jobDefinition;

    private List<JobDefinition> jobDefinitions;

    public JobDefinitionReader() {
        jobDefinitions = new ArrayList<>();
    }

    public List<JobDefinition> getJobDefinitions() {
        if (jobDefinitions.isEmpty())
            readJobDefinitionFromJson();
        return jobDefinitions;
    }

    private void readJobDefinitionFromJson() {
        JobDefinition[] jobDefinitions = new Gson().fromJson(
                BatchUtils.convertResourceOutputToString(jobDefinition), JobDefinition[].class);
        if (jobDefinitions != null) {
            this.jobDefinitions = Arrays.asList(jobDefinitions);
        }
    }

    public String getConceptReferenceSource() {
        Optional<String> code = jobDefinitions.stream()
                .filter(job -> job.getType().equals("obs"))
                .map(JobDefinition::getConceptReferenceSource).filter(Objects::nonNull).findFirst();
        return code.orElse("");
    }

    public JobDefinition getJobDefinitionByName(String jobName) {
        Optional<JobDefinition> optionalJobDefinition = getJobDefinitions().stream()
                .filter(tempJobDefinition -> tempJobDefinition.getName().equals(jobName)).findFirst();
        return optionalJobDefinition.orElseGet(JobDefinition::new);
    }
}
