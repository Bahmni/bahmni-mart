package org.bahmni.mart.config;

import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.group.GroupedJob;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.procedure.ProcedureDefinition;
import org.bahmni.mart.config.view.ViewDefinition;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private GroupedJob groupedJob;

    public List<JobDefinition> getJobDefinitions() {
        List<JobDefinition> jobDefinitions = bahmniMartJSON.getJobs();

        List<JobDefinition> groupedJobDefinitions = getGroupedJobDefinitions();

        List<JobDefinition> allJobDefinitions = new ArrayList<>(jobDefinitions);
        allJobDefinitions.addAll(groupedJobDefinitions);


        return CollectionUtils.isEmpty(allJobDefinitions) ? new ArrayList<>() : allJobDefinitions;
    }

    public List<JobDefinition> getJobDefinitions(Resource resource) {
        String json = BatchUtils.convertResourceOutputToString(resource);
        BahmniMartJSON bahmniMartJSON = new Gson().fromJson(json, BahmniMartJSON.class);
        if (bahmniMartJSON == null) {
            return new ArrayList<>();
        }
        return bahmniMartJSON.getJobs();
    }

    public List<JobDefinition> getJobDefinitionsFromBahmniMartJson() {
        List<JobDefinition> jobDefinitions = bahmniMartJSON.getJobs();
        return CollectionUtils.isEmpty(jobDefinitions) ? new ArrayList<>() : jobDefinitions;
    }

    public List<ViewDefinition> getViewDefinitions() {
        List<ViewDefinition> viewDefinitions = bahmniMartJSON.getViews();
        return CollectionUtils.isEmpty(viewDefinitions) ? new ArrayList<>() : viewDefinitions;
    }

    public List<ProcedureDefinition> getProcedureDefinitions() {
        List<ProcedureDefinition> procedureDefinitions = bahmniMartJSON.getProcedures();
        return CollectionUtils.isEmpty(procedureDefinitions) ? new ArrayList<>() : procedureDefinitions;
    }

    private List<JobDefinition> getGroupedJobDefinitions() {
        List<JobDefinition> groupedJobDefinitions = new ArrayList<>();
        List<JobDefinition> jobDefinitionsByGroupedJobTypes = groupedJob.getGroupedTypeJobDefinitions();
        jobDefinitionsByGroupedJobTypes.forEach(jobDefinition -> {
            groupedJobDefinitions.addAll(groupedJob.getJobDefinitions(jobDefinition));
        });
        return groupedJobDefinitions;
    }


    @PostConstruct
    public void read() {
        if (bahmniMartJSON == null) {
            String json = BatchUtils.convertResourceOutputToString(configSource);
            bahmniMartJSON = new Gson().fromJson(json, BahmniMartJSON.class);
        }
    }

}
