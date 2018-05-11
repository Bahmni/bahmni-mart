package org.bahmni.mart.form;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.bahmni.mart.helper.IgnoreColumnsConfigHelper;
import org.bahmni.mart.helper.SeparateTableConfigHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Component
public class BahmniFormFactory {

    private static final List<Concept> EMPTY_LIST = Collections.emptyList();

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private SeparateTableConfigHelper separateTableConfigHelper;

    @Autowired
    private IgnoreColumnsConfigHelper ignoreColumnsConfigHelper;

    private List<Concept> allSeparateTableConcepts;

    public BahmniForm createForm(Concept concept, BahmniForm parentForm, JobDefinition jobDefinition) {
        return createForm(concept, parentForm, 0,
                ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition));
    }

    private BahmniForm createForm(Concept concept, BahmniForm parentForm, int depth, HashSet<Concept> ignoreConcepts) {
        BahmniForm bahmniForm = new BahmniForm();
        bahmniForm.setFormName(concept);
        bahmniForm.setDepthToParent(depth);
        bahmniForm.setParent(parentForm);
        bahmniForm.setRootForm(getRootFormFor(parentForm));

        constructFormFields(concept, bahmniForm, depth, ignoreConcepts);
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

    private void constructFormFields(Concept concept, BahmniForm bahmniForm, int depth,
                                     HashSet<Concept> ignoreConcepts) {
        if (concept.getIsSet() == 0) {
            bahmniForm.addField(concept);
            return;
        }

        List<Concept> childConcepts = conceptService.getChildConcepts(concept.getName());
        int childDepth = depth + 1;
        for (Concept childConcept : childConcepts) {
            if (ignoreConcepts.contains(childConcept)) {
                continue;
            } else if (allSeparateTableConcepts.contains(childConcept)) {
                bahmniForm.addChild(createForm(childConcept, bahmniForm, childDepth, ignoreConcepts));
            } else if (childConcept.getIsSet() == 0) {
                bahmniForm.addField(childConcept);
            } else {
                constructFormFields(childConcept, bahmniForm, childDepth, ignoreConcepts);
            }
        }
    }

    @PostConstruct
    public void postConstruct() {
        this.allSeparateTableConcepts = getSeparateTableConcepts();
    }

    private List<Concept> getSeparateTableConcepts() {
        List<String> conceptNames = separateTableConfigHelper.getAllSeparateTableConceptNames();
        return conceptNames.isEmpty() ? EMPTY_LIST : conceptService.getConceptsByNames(conceptNames);
    }
}
