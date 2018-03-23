package org.bahmni.mart.form;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.bahmni.mart.helper.SeparateTableConfigHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForObsJob;

@Component
public class BahmniFormFactory {

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
        this.allSeparateTableConcepts = obsService.getConceptsByNames(allSeparateTableConceptNames);
        this.ignoreConcepts = obsService.getConceptsByNames(getIgnoreConceptNamesForObsJob(
                jobDefinitionReader.getJobDefinitions()));
    }

    public void setObsService(ObsService obsService) {
        this.obsService = obsService;
    }
}
