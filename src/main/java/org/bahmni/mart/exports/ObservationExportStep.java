package org.bahmni.mart.exports;

import org.springframework.batch.core.Step;

public interface ObservationExportStep {
    Step getStep();

    Step getRemovalStep();
}
