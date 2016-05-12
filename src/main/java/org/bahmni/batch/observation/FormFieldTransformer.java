package org.bahmni.batch.observation;

import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Form;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FormFieldTransformer {

	public List<Integer> transformFormToFieldIds(Form form){
		List<Integer> fieldIds = new ArrayList<>();

		for(Concept field: form.getFields()){
			fieldIds.add(field.getId());
		}

		return fieldIds;
	}


}
