package org.bahmni.mart.exports;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class TreatmentRegistrationBaseExportStep extends BaseExportStep {

    @Autowired
    public TreatmentRegistrationBaseExportStep(StepBuilderFactory stepBuilderFactory, DataSource dataSource,
                                               @Value("classpath:sql/treatmentRegistration.sql") Resource sqlResource,
                                               @Value("${outputFolder}/treatmentRegistration.csv")
                                                       Resource outputFolder,
                                               @Value("${treatmentRegistrationHeaders}") String headers) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "treatmentRegistration", headers);
    }
}
