package org.bahmni.batch.observation;

import org.bahmni.batch.Application;
import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.observation.domain.Concept;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource(locations="classpath:test.properties")
public class DynamicObsQueryTest {

	@Autowired
	private DynamicObsQuery dynamicObsQuery;

	@Test
	public void ensureSqlQueryDoesNotIncludeParentInTopLevelForms(){
		BahmniForm parent = new BahmniForm();
		parent.setParent(null);
		parent.setFormName(new Concept(1189,"Vitals",1));
		parent.setDepthToParent(0);

		String sql = dynamicObsQuery.getSqlQueryForForm(parent);
		System.out.println(sql);
		assertEquals("SELECT obs0.obs_id,obs0.obs_id as parent_obs_id\n" + "FROM obs obs0\n" + "WHERE obs0.concept_id=1189\n"
				+ "AND obs0.voided = 0\n",sql);
	}

	@Test
	public void ensureParentWithDepth1IsConstructed(){
		BahmniForm parent = new BahmniForm();
		parent.setParent(null);
		parent.setFormName(new Concept(1,"Vitals",1));
		parent.setDepthToParent(0);

		BahmniForm child = new BahmniForm();
		child.setParent(parent);
		child.setFormName(new Concept(10,"Systolic",1));
		child.setDepthToParent(1);


		String sql = dynamicObsQuery.getSqlQueryForForm(child);
		System.out.println(sql);
		assertEquals("SELECT obs0.obs_id,obs1.obs_id as parent_obs_id\n" + "FROM obs obs0\n"
				+ "INNER JOIN obs obs1 on ( obs1.obs_id=obs0.obs_group_id and obs1.voided=0 )\n"
				+ "WHERE obs0.concept_id=10\n" + "AND obs0.voided = 0\n" + "AND obs1.concept_id=1\n",sql);
	}

	@Test
	public void ensureParentWithDepth2IsConstructed(){
		BahmniForm parent = new BahmniForm();
		parent.setParent(null);
		parent.setFormName(new Concept(1,"Vitals",1));
		parent.setDepthToParent(0);

		BahmniForm child = new BahmniForm();
		child.setParent(parent);
		child.setFormName(new Concept(10,"Systolic",1));
		child.setDepthToParent(2);


		String sql = dynamicObsQuery.getSqlQueryForForm(child);
		System.out.println(sql);
		assertEquals("SELECT obs0.obs_id,obs2.obs_id as parent_obs_id\n" + "FROM obs obs0\n"
				+ "INNER JOIN obs obs1 on ( obs1.obs_id=obs0.obs_group_id and obs1.voided=0 )\n"
				+ "INNER JOIN obs obs2 on ( obs2.obs_id=obs1.obs_group_id and obs2.voided=0 )\n"
				+ "WHERE obs0.concept_id=10\n" + "AND obs0.voided = 0\n" + "AND obs2.concept_id=1\n",sql);
	}


}
