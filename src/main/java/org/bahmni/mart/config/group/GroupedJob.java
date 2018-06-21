package org.bahmni.mart.config.group;

import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.job.JobDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.config.job.JobDefinitionUtil.setCommonPropertiesToGroupedJobs;
import static org.bahmni.mart.config.job.JobDefinitionUtil.setConfigToGroupedJobs;

@Component
public class GroupedJob {

    @Autowired
    private MartJSONReader martJSONReader;

    @Autowired
    private ResourceLoader resourceLoader;

    public List<JobDefinition> getJobDefinitions(JobDefinition jobDefinition) {

        List<JobDefinition> groupedJobDefinitions = getGroupedJobDefinitions(jobDefinition);

        setCommonPropertiesToGroupedJobs(jobDefinition, groupedJobDefinitions);
        setConfigToGroupedJobs(jobDefinition, groupedJobDefinitions);

        return groupedJobDefinitions;
    }

    private List<JobDefinition> getGroupedJobDefinitions(JobDefinition jobDefinition) {
        Resource groupedJsonResource = getGroupedJsonResource(jobDefinition);
        return martJSONReader.getJobDefinitions(groupedJsonResource);
    }

    private Resource getGroupedJsonResource(JobDefinition jobDefinition) {
        String type = jobDefinition.getType();
        String jsonClassPath = "classpath:groupedModules/" + type + ".json";

        return resourceLoader.getResource(jsonClassPath);
    }

    public List<JobDefinition> getJobDefinitionsBySkippingGroupedTypeJobs(List<JobDefinition> allJobDefinitions) {
        return allJobDefinitions.stream().filter(jobDefinition -> !GroupedJobType.contains(jobDefinition.getType()))
                .collect(Collectors.toList());
    }

    public List<JobDefinition> getGroupedTypeJobDefinitions() {
        return martJSONReader.getJobDefinitionsFromBahmniMartJson().stream()
                .filter(jobDefinition -> GroupedJobType.contains(jobDefinition.getType())).collect(Collectors.toList());
    }
}
