package org.bahmni.mart.config.job;

import com.google.gson.Gson;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.exports.SimpleJobTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class JobDefinitionReader {

    @Autowired
    private SimpleJobTemplate simpleJobTemplate;

    @Value("${bahmniMartConfigPath}")
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
}
