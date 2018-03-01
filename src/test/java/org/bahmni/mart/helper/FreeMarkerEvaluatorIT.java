package org.bahmni.mart.helper;

import org.bahmni.mart.AbstractBaseBatchIT;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class FreeMarkerEvaluatorIT extends AbstractBaseBatchIT {

    @Autowired
    private FreeMarkerEvaluator<BahmniForm> dynamicObsQuery;

    @Test
    public void ensureSqlQueryDoesNotIncludeParentInTopLevelForms() {
        BahmniForm parent = new BahmniForm();
        parent.setParent(null);
        parent.setFormName(new Concept(1189, "Vitals", 1));
        parent.setDepthToParent(0);

        String sql = dynamicObsQuery.evaluate("obsWithParentSql.ftl", parent);
        assertEquals("SELECT obs0.obs_id FROM obs obs0 WHERE obs0.concept_id =1189 AND obs0.voided = 0",
                sql.trim());
    }

    @Test
    public void ensureParentWithDepth1IsConstructed() {
        BahmniForm parent = new BahmniForm();
        parent.setParent(null);
        parent.setFormName(new Concept(1, "Vitals", 1));
        parent.setDepthToParent(0);

        BahmniForm child = new BahmniForm();
        child.setParent(parent);
        child.setFormName(new Concept(10, "Systolic", 1));
        child.setDepthToParent(1);


        String sql = dynamicObsQuery.evaluate("obsWithParentSql.ftl", child);
        assertEquals("SELECT obs0.obs_id FROM obs obs0 WHERE obs0.concept_id =10 AND obs0.voided = 0",
                sql.trim());
    }

    @Test
    public void ensureParentWithDepth2IsConstructed() {
        BahmniForm parent = new BahmniForm();
        parent.setParent(null);
        parent.setFormName(new Concept(1, "Vitals", 1));
        parent.setDepthToParent(0);

        BahmniForm child = new BahmniForm();
        child.setParent(parent);
        child.setFormName(new Concept(10, "Systolic", 1));
        child.setRootForm(parent);
        child.setDepthToParent(2);


        String sql = dynamicObsQuery.evaluate("obsWithParentSql.ftl", child);
        assertEquals("SELECT obs0.obs_id , obs1.obs_id as parent_obs_id FROM obs obs0 INNER JOIN obs " +
                "obs1 on (obs1.obs_id = obs0.obs_group_id and obs1.voided = 0) WHERE obs0.concept_id =10 AND " +
                "obs0.voided = 0 AND obs1.concept_id =1 AND obs1.concept_id =1", sql.trim());
    }

    @Test
    public void ensureParentWithDepthIsMoreThan2Constructed() {
        BahmniForm geandParent = new BahmniForm();
        geandParent.setParent(null);
        geandParent.setFormName(new Concept(1, "Vitals", 1));
        geandParent.setDepthToParent(0);

        BahmniForm parent = new BahmniForm();
        parent.setParent(geandParent);
        parent.setFormName(new Concept(10, "Systolic", 1));
        parent.setRootForm(geandParent);
        parent.setDepthToParent(2);

        BahmniForm child = new BahmniForm();
        child.setParent(parent);
        child.setFormName(new Concept(12, "Systolic Notes", 0));
        child.setRootForm(parent);
        child.setDepthToParent(3);


        String sql = dynamicObsQuery.evaluate("obsWithParentSql.ftl", child);
        assertEquals("SELECT obs0.obs_id , obs1.obs_id as parent_obs_id FROM obs obs0 INNER JOIN obs obs1 " +
                "on (obs1.obs_id = obs0.obs_group_id and obs1.voided = 0) INNER JOIN obs obs2 on " +
                "(obs2.obs_id = obs1.obs_group_id and obs2.voided = 0) WHERE obs0.concept_id =12 " +
                "AND obs0.voided = 0 AND obs1.concept_id =10 AND obs2.concept_id =10", sql.trim());
    }
}

