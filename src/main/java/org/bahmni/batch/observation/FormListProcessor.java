package org.bahmni.batch.observation;

import org.bahmni.batch.form.BahmniFormFactory;
import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.ObsService;
import org.bahmni.batch.observation.domain.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class FormListProcessor {

	public static final String ALL_FORMS = "All Observation Templates";
	public static final String BACTERIOLOGY_CONCEPT_SET = "Bacteriology Concept Set";

	@Autowired
	private ObsService obsService;

	@Autowired
	private BahmniFormFactory bahmniFormFactory;


	public List<BahmniForm> retrieveAllForms(){
		List<Concept> allFormConcepts = obsService.getChildConcepts(ALL_FORMS);
		allFormConcepts.add(obsService.getConceptsByNames(BACTERIOLOGY_CONCEPT_SET).get(0));

		List<BahmniForm> forms = new ArrayList<>();
		for(Concept concept: allFormConcepts){
			forms.add(bahmniFormFactory.createForm(concept,null));
		}

		List<BahmniForm> flattenedFormList = new ArrayList<>(forms);
		fetchExportFormsList(forms, flattenedFormList);
		return flattenedFormList;
	}

	private void fetchExportFormsList(List<BahmniForm> forms, List<BahmniForm> flattenedList){
		for(BahmniForm form : forms){
			if(form.getChildren().size()!=0) {
				flattenedList.addAll(form.getChildren());
				fetchExportFormsList(form.getChildren(), flattenedList);
			}
		}
	}

	public void setObsService(ObsService obsService) {
		this.obsService = obsService;
	}

	public void setBahmniFormFactory(BahmniFormFactory bahmniFormFactory) {
		this.bahmniFormFactory = bahmniFormFactory;
	}
}
