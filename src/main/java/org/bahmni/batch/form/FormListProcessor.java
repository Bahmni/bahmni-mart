package org.bahmni.batch.form;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.Concept;
import org.bahmni.batch.form.service.ObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class FormListProcessor {

	public static final String ALL_FORMS = "All Observation Templates";

	@Autowired
	private ObsService obsService;

	@Autowired
	private BahmniFormFactory bahmniFormFactory;


	public List<BahmniForm> retrieveAllForms(){
		List<Concept> allFormConcepts = obsService.getChildConcepts(ALL_FORMS);
		allFormConcepts.add(obsService.getConceptsByNames("Bacteriology Concept Set").get(0));

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
