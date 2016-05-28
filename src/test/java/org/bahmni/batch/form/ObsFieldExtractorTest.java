package org.bahmni.batch.form;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.Concept;
import org.bahmni.batch.form.domain.Obs;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObsFieldExtractorTest {

	@Test
	public void shouldExtractObsListToObjectArray(){
		BahmniForm form = new BahmniForm();
		form.setFormName(new Concept(0,"Blood Pressure",1));
		form.addField(new Concept(1,"Systolic",0));
		form.addField(new Concept(2,"Diastolic",0));
		ObsFieldExtractor fieldExtractor = new ObsFieldExtractor(form);

		List<Obs> obsList = new ArrayList<>();
		obsList.add(new Obs("AB1234",1,0, new Concept(1,"Systolic",0),"120"));
		obsList.add(new Obs("AB1234",1,0, new Concept(2,"Diastolic",0),"80"));

		List<Object> result = Arrays.asList(fieldExtractor.extract(obsList));


		assertEquals(4,result.size());
		assertTrue(result.contains("120"));
		assertTrue(result.contains("80"));
	}

	@Test
	public void ensureThatSplCharsAreHandledInCSVInTheObsValue(){
		BahmniForm form = new BahmniForm();
		form.setFormName(new Concept(0,"Blood Pressure",1));
		form.addField(new Concept(1,"Systolic",0));
		form.addField(new Concept(2,"Diastolic",0));

		ObsFieldExtractor fieldExtractor = new ObsFieldExtractor(form);

		List<Obs> obsList = new ArrayList<>();
		obsList.add(new Obs("AB1234",1,0, new Concept(1,"Systolic",0),"abc\ndef\tghi,klm"));

		Object[] result = fieldExtractor.extract(obsList);

		assertEquals(new Integer(1),result[0]);
		assertEquals("AB1234",result[1]);
		assertEquals("abc def ghi klm",result[2]);
	}

}
