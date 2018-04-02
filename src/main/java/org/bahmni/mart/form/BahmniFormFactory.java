package org.bahmni.mart.form;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.bahmni.mart.helper.SeparateTableConfigHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForObsJob;

@Component
public class BahmniFormFactory {

    private static final String OBS_JOB_TYPE = "obs";

    @Autowired
    private ObsService obsService;

    @Autowired
    private SeparateTableConfigHelper separateTableConfigHelper;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    private List<Concept> allSeparateTableConcepts;
    private List<Concept> ignoreConcepts;

    public BahmniForm createForm(Concept concept, BahmniForm parentForm) {
        return createForm(concept, parentForm, 0);
    }

    private BahmniForm createForm(Concept concept, BahmniForm parentForm, int depth) {
        BahmniForm bahmniForm = new BahmniForm();
        bahmniForm.setFormName(concept);
        bahmniForm.setDepthToParent(depth);
        bahmniForm.setParent(parentForm);

        constructFormFields(concept, bahmniForm, depth);
        return bahmniForm;
    }

    private void constructFormFields(Concept concept, BahmniForm bahmniForm, int depth) {
        if (concept.getIsSet() == 0) {
            bahmniForm.addField(concept);
            return;
        }

        List<Concept> childConcepts = obsService.getChildConcepts(concept.getName());
        int childDepth = depth + 1;
        for (Concept childConcept : childConcepts) {
            if (ignoreConcepts.contains(childConcept)) {
                continue;
            } else if (allSeparateTableConcepts.contains(childConcept)) {
                bahmniForm.addChild(createForm(childConcept, bahmniForm, childDepth));
            } else if (childConcept.getIsSet() == 0) {
                bahmniForm.addField(childConcept);
            } else {
                constructFormFields(childConcept, bahmniForm, childDepth);
            }
        }
    }

    @PostConstruct
    public void postConstruct() {
        List<String> allSeparateTableConceptNames = separateTableConfigHelper.getAllSeparateTableConceptNames();
        List<JobDefinition> jobDefinitions = jobDefinitionReader.getJobDefinitions();

        this.allSeparateTableConcepts = isObsJobPresent(jobDefinitions) ?
                obsService.getConceptsByNames(allSeparateTableConceptNames) : Collections.emptyList();
        this.ignoreConcepts = isObsJobWithOutIgnoreColumns(jobDefinitions) ? Collections.emptyList() :
                obsService.getConceptsByNames(getIgnoreConceptNamesForObsJob(jobDefinitions));
    }

    private Boolean isObsJobWithOutIgnoreColumns(List<JobDefinition> jobDefinitions) {
        return getIgnoreConceptNamesForObsJob(jobDefinitions).isEmpty();
    }

    private Boolean isObsJobPresent(List<JobDefinition> jobDefinitions) {
        return !jobDefinitions.stream()
                .filter(jobDefinition -> OBS_JOB_TYPE.equals(jobDefinition.getType()))
                .collect(Collectors.toList()).isEmpty();
    }
}
