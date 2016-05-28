package org.bahmni.batch.form;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.Concept;
import org.bahmni.batch.form.domain.Obs;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.util.StringUtils;

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

		row.add(obsList.get(0).getId());

		if(form.getParent()!=null){
			row.add(obsList.get(0).getParentId());
		}

		row.add(obsList.get(0).getTreatmentNumber());

		for(Concept field: form.getFields()){
			row.add(massageStringValue(obsRow.get(field)));
		}

		return row.toArray();
	}

	private String massageStringValue(String text){
		if(StringUtils.isEmpty(text))
			return text;
		return text.replaceAll("\n"," ").replaceAll("\t"," ").replaceAll(","," ");
	}
}
