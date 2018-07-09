package org.bahmni.mart.helper;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForJob;

@Component
public class IgnoreColumnsConfigHelper {

    @Autowired
    private ConceptService conceptService;

    private HashMap<String, HashSet<Concept>> ignoreConceptsMap = new HashMap<>();

    public HashSet<Concept> getIgnoreConceptsForJob(JobDefinition jobDefinition) {
        String jobName = jobDefinition.getName();
        if (!ignoreConceptsMap.containsKey(jobName))
            ignoreConceptsMap.put(jobName, getIgnoreConcepts(jobDefinition));

        return ignoreConceptsMap.get(jobName);
    }

    private HashSet<Concept> getIgnoreConcepts(JobDefinition jobDefinition) {
        HashSet<Concept> ignoreConcepts = new HashSet<>();

        if (!isJobWithOutIgnoreColumns(jobDefinition))
            ignoreConcepts.addAll(conceptService.getConceptsByNames(jobDefinition.getColumnsToIgnore()));

        if (jobDefinition.getIgnoreAllFreeTextConcepts())
            ignoreConcepts.addAll(conceptService.getFreeTextConcepts());
        return ignoreConcepts;
    }

    private Boolean isJobWithOutIgnoreColumns(JobDefinition jobDefinition) {
        return getIgnoreConceptNamesForJob(jobDefinition).isEmpty();
    }
}
