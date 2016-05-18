package org.bahmni.batch.exports;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.observation.domain.Form;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObservationExportStepTest {
    ObservationExportStep observationExportStep = new ObservationExportStep();
    @Test
    public void  shouldHaveThreesSelfJoinsWhenDepthIsThree() throws Exception {
        BahmniForm form = new BahmniForm();
        form.setDepthToParent(3);
        observationExportStep.setForm(form);
        String expectedSql = "SELECT  obs0.obs_id,obs3.obs_id FROM obs obs0  JOIN obs obs1 on ( obs1.obs_id = obs0.obs_group_id ) JOIN obs obs2 on ( obs2.obs_id = obs1.obs_group_id ) JOIN obs obs3 on ( obs3.obs_id = obs2.obs_group_id )";
        String addMoreSql = observationExportStep.constructSql();
        Assert.assertEquals(expectedSql,addMoreSql);

    }

    @Test
    public void  shouldHaveOneSelfJoinsWhenDepthIsOne() throws Exception {
        BahmniForm form = new BahmniForm();
        form.setDepthToParent(1);
        observationExportStep.setForm(form);
        String expectedSql = "SELECT  obs0.obs_id,obs1.obs_id FROM obs obs0  JOIN obs obs1 on ( obs1.obs_id = obs0.obs_group_id )" ;
        String addMoreSql = observationExportStep.constructSql();
        Assert.assertEquals(expectedSql,addMoreSql);

    }
}
