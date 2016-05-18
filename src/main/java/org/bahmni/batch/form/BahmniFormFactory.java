package org.bahmni.batch.form;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.ObsService;
import org.bahmni.batch.observation.domain.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class BahmniFormFactory {

	@Value("${addMoreConcepts}")
	private String addMoreConceptNames;

	@Autowired
	private ObsService obsService;

	private List<Concept> addMoreConcepts;

	public BahmniForm createForm(Concept concept, BahmniForm parentForm) {
		return createForm(concept,parentForm,0);
	}

	private BahmniForm createForm(Concept concept, BahmniForm parentForm, int depth){
		BahmniForm bahmniForm = new BahmniForm();
		bahmniForm.setFormName(concept);
		bahmniForm.setDepthToParent(depth);
		bahmniForm.setParent(parentForm);

		constructFormFields(concept, bahmniForm, 0);

		return bahmniForm;
	}

	private void constructFormFields(Concept concept, BahmniForm bahmniForm, int depth) {
		if(concept.getIsSet() == 0){
			bahmniForm.addField(concept);
			return;
		}

		List<Concept> childConcepts = obsService.getChildConcepts(concept.getName());
		depth++;
		for(Concept childConcept: childConcepts){

			if(addMoreConcepts.contains(childConcept)){
				bahmniForm.addChild(createForm(childConcept, bahmniForm, depth));
			}else if(childConcept.getIsSet() == 0){
				bahmniForm.addField(childConcept);
			}else{
				constructFormFields(childConcept,bahmniForm, depth);
			}
		}
	}

	@PostConstruct
	public void postConstruct(){
		this.addMoreConcepts = obsService.getConceptsByNames(addMoreConceptNames);
	}

	public void setObsService(ObsService obsService) {
		this.obsService = obsService;
	}
}
