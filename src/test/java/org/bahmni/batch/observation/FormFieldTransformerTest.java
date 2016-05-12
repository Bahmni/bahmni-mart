package org.bahmni.batch.observation;

import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Form;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FormFieldTransformerTest {

	@Test
	public void shouldTransformFormFieldsToIds(){
		Form form = new Form();
		form.setFormName(new Concept(0,"ParentConcept",1));
		form.addField(new Concept(1,"headache",0));
		form.addField(new Concept(2,"systolic",0));
		form.addField(new Concept(3,"diastolic",0));

		FormFieldTransformer formFieldTransformer = new FormFieldTransformer();
		List<Integer> ids = formFieldTransformer.transformFormToFieldIds(form);
		assertEquals(3,ids.size());
		assertEquals(new Integer(1),ids.get(0));
		assertEquals(new Integer(2),ids.get(1));
		assertEquals(new Integer(3),ids.get(2));
	}

}
