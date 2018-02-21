package org.bahmni.analytics.form;

import org.bahmni.analytics.form.domain.BahmniForm;
import org.bahmni.analytics.form.domain.Concept;
import org.bahmni.analytics.form.service.ObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class BahmniFormFactory {

    @Value("${addMoreAndMultiSelectConcepts}")
    private String addMoreConceptNames;

    @Value("${ignoreConcepts}")
    private String ignoreConceptsNames;

    @Autowired
    private ObsService obsService;

    private List<Concept> addMoreAndMultiSelectConcepts;
    private List<Concept> ignoreConcepts;

    public BahmniForm createForm(Concept concept, BahmniForm parentForm) {
        return createForm(concept, parentForm, 0);
    }

    private BahmniForm createForm(Concept concept, BahmniForm parentForm, int depth) {
        BahmniForm bahmniForm = new BahmniForm();
        bahmniForm.setFormName(concept);
        bahmniForm.setDepthToParent(depth);
        bahmniForm.setParent(parentForm);

        constructFormFields(concept, bahmniForm, 0);
        return bahmniForm;
    }

    private void constructFormFields(Concept concept, BahmniForm bahmniForm, int depth) {
        if (concept.getIsSet() == 0)
            bahmniForm.addField(concept);
        else
            obsService.getChildConcepts(concept.getName())
                    .forEach(childConcept -> addChild(bahmniForm, childConcept, depth + 1));
    }

    private void addChild(BahmniForm bahmniForm, Concept childConcept, int childDepth) {
        if (ignoreConcepts.contains(childConcept))
            return;
        else if (addMoreAndMultiSelectConcepts.contains(childConcept))
            bahmniForm.addChild(createForm(childConcept, bahmniForm, childDepth));
        else
            constructFormFields(childConcept, bahmniForm, childDepth);
    }

    @PostConstruct
    public void postConstruct() {
        this.addMoreAndMultiSelectConcepts = obsService.getConceptsByNames(addMoreConceptNames);
        this.ignoreConcepts = obsService.getConceptsByNames(ignoreConceptsNames);
    }

    public void setObsService(ObsService obsService) {
        this.obsService = obsService;
    }
}
