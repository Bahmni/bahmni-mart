package org.bahmni.mart.helper;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForJob;

@Component
public class IgnoreColumnsConfigHelper {

    @Autowired
    private ObsService obsService;

    private Map<String, List<Concept>> ignoreConceptsMap = new HashMap<>();

    public List<Concept> getIgnoreConceptsForJob(JobDefinition jobDefinition) {
        String jobName = jobDefinition.getName();
        if (!ignoreConceptsMap.containsKey(jobName))
            ignoreConceptsMap.put(jobName, getIgnoreConcepts(jobDefinition));

        return ignoreConceptsMap.get(jobName);
    }

    private ArrayList<Concept> getIgnoreConcepts(JobDefinition jobDefinition) {
        ArrayList<Concept> ignoreConcepts = new ArrayList<>();

        if (!isJobWithOutIgnoreColumns(jobDefinition))
            ignoreConcepts.addAll(obsService.getConceptsByNames(jobDefinition.getColumnsToIgnore()));

        if (jobDefinition.getIgnoreAllFreeTextConcepts())
            ignoreConcepts.addAll(obsService.getFreeTextConcepts());
        return ignoreConcepts;
    }

    private Boolean isJobWithOutIgnoreColumns(JobDefinition jobDefinition) {
        return getIgnoreConceptNamesForJob(jobDefinition).isEmpty();
    }
}
