package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.springframework.batch.core.Step;

public interface ObservationExportStep {
    Step getStep();

    Step getRemovalStep();

    void setJobDefinition(JobDefinition jobDefinition);

    void setForm(BahmniForm form);
}
