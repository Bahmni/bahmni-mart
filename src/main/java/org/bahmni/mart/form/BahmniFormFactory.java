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

import static org.bahmni.mart.config.job.JobDefinitionUtil.BACTERIOLOGY_JOB_TYPE;
import static org.bahmni.mart.config.job.JobDefinitionUtil.OBS_JOB_TYPE;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForJob;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;

@Component
public class BahmniFormFactory {

    private static final List<Concept> EMPTY_LIST = Collections.emptyList();

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
        bahmniForm.setRootForm(getRootFormFor(parentForm));

        constructFormFields(concept, bahmniForm, depth);
        return bahmniForm;
    }

    private BahmniForm getRootFormFor(BahmniForm form) {
        if (form == null) {
            return null;
        } else if (form.getDepthToParent() == 0) {
            return form;
        }
        return getRootFormFor(form.getParent());
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
        JobDefinition obsJobDefinition = getJobDefinitionByType(jobDefinitions, OBS_JOB_TYPE);
        JobDefinition bacteriologyJobDefinition = getJobDefinitionByType(jobDefinitions, BACTERIOLOGY_JOB_TYPE);

        this.allSeparateTableConcepts = allSeparateTableConceptNames.isEmpty() ? EMPTY_LIST :
                obsService.getConceptsByNames(allSeparateTableConceptNames);
        this.ignoreConcepts = isJobWithOutIgnoreColumns(obsJobDefinition) ? EMPTY_LIST :
                obsService.getConceptsByNames(getIgnoreConceptNamesForJob(obsJobDefinition));
        List<Concept> bacteriologyIgnoreConcepts = isJobWithOutIgnoreColumns(bacteriologyJobDefinition) ?
                EMPTY_LIST : obsService.getConceptsByNames(getIgnoreConceptNamesForJob(bacteriologyJobDefinition));
        ignoreConcepts.addAll(bacteriologyIgnoreConcepts);
        if (obsJobDefinition.getIgnoreAllFreeTextConcepts())
            ignoreConcepts.addAll(obsService.getFreeTextConcepts());
    }

    private Boolean isJobWithOutIgnoreColumns(JobDefinition jobDefinition) {
        return getIgnoreConceptNamesForJob(jobDefinition).isEmpty();
    }
}
