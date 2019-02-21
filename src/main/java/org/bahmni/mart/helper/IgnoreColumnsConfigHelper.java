package org.bahmni.mart.helper;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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

        HashSet<Concept> freeTextConceptsToBeIgnored = getAllFreeTextConceptsToBeIgnored(jobDefinition);
        ignoreConcepts.addAll(freeTextConceptsToBeIgnored);
        return ignoreConcepts;
    }

    private HashSet<Concept> getAllFreeTextConceptsToBeIgnored(JobDefinition jobDefinition) {
        HashSet<Concept> textConceptsToBeIgnored = new HashSet<>();
        if (jobDefinition.getIgnoreAllFreeTextConcepts()) {
            List<Concept> allFreeTextConcepts = conceptService.getFreeTextConcepts();
            textConceptsToBeIgnored.addAll(allFreeTextConcepts);
            List<Concept> textConceptsToInclude = new ArrayList<>();
            if (!Objects.isNull(jobDefinition.getIncludeFreeTextConceptNames()) &&
                    jobDefinition.getIncludeFreeTextConceptNames().size() > 0) {
                textConceptsToInclude = conceptService
                        .getConceptsByNames(jobDefinition.getIncludeFreeTextConceptNames());
            }
            textConceptsToBeIgnored.removeAll(textConceptsToInclude);
        }
        return textConceptsToBeIgnored;
    }

    private Boolean isJobWithOutIgnoreColumns(JobDefinition jobDefinition) {
        return getIgnoreConceptNamesForJob(jobDefinition).isEmpty();
    }
}
