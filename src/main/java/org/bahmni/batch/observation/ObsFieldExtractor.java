package org.bahmni.batch.observation;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Form;
import org.bahmni.batch.observation.domain.Obs;
import org.springframework.batch.item.file.transform.FieldExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObsFieldExtractor implements FieldExtractor<List<Obs>>{

	private BahmniForm form;

	public ObsFieldExtractor(BahmniForm form){
		this.form = form;
	}

	@Override
	public Object[] extract(List<Obs> obsList) {
		List<Object> row = new ArrayList<>();

		if(obsList.size()==0)
			return row.toArray();


		Map<Concept,String> obsRow = new HashMap<>();
		for(Obs obs: obsList){
			obsRow.put(obs.getField(),obs.getValue());
		}

		row.add(obsList.get(0).getTreatmentNumber());
		row.add(obsList.get(0).getId());
		row.add(obsList.get(0).getParentId()); //TODO: this should be optional based on parentConcept available or not

		for(Concept field: form.getFields()){
			row.add(obsRow.get(field));
		}

		return row.toArray();
	}
}
