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
        return CollectionUtils.isEmpty(bahmniMartJSON.getJobs()) ? new ArrayList<>() : bahmniMartJSON.getJobs();
    }

    public List<ViewDefinition> getViewDefinitions() {
        return CollectionUtils.isEmpty(bahmniMartJSON.getViews()) ? new ArrayList<>() : bahmniMartJSON.getViews();
    }

    @PostConstruct
    public void read() {
        if (bahmniMartJSON == null) {
            String json = BatchUtils.convertResourceOutputToString(configSource);
            bahmniMartJSON = new Gson().fromJson(json, BahmniMartJSON.class);
        }
    }

}
