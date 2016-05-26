package org.bahmni.batch.observation;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Obs;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

		Object[] result = fieldExtractor.extract(obsList);

		assertEquals(5,result.length);
		assertEquals("120",result[3]);
		assertEquals("80",result[4]);
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
