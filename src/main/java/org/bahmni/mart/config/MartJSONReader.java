package org.bahmni.mart.config;

import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.view.ViewDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MartJSONReader {

    @Value("${bahmniMartConfigFile}")
    private Resource configSource;

    private static BahmniMartJSON bahmniMartJSON;

    public List<JobDefinition> getJobDefinitions() {
        List<JobDefinition> jobDefinitions = bahmniMartJSON.getJobs();
        return CollectionUtils.isEmpty(jobDefinitions) ? new ArrayList<>() : jobDefinitions;
    }

    public List<ViewDefinition> getViewDefinitions() {
        List<ViewDefinition> viewDefinitions = bahmniMartJSON.getViews();
        return CollectionUtils.isEmpty(viewDefinitions) ? new ArrayList<>() : viewDefinitions;
    }

    @PostConstruct
    public void read() {
        if (bahmniMartJSON == null) {
            String json = BatchUtils.convertResourceOutputToString(configSource);
            bahmniMartJSON = new Gson().fromJson(json, BahmniMartJSON.class);
        }
    }

}
