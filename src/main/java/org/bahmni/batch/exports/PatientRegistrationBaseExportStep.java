package org.bahmni.batch.exports;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class PatientRegistrationBaseExportStep extends BaseExportStep {

    @Autowired
    public PatientRegistrationBaseExportStep(StepBuilderFactory stepBuilderFactory, DataSource dataSource,
                                             @Value("classpath:sql/patientRegistration.sql") Resource sqlResource,
                                             @Value("${outputFolder}/patientRegistration.csv") Resource outputFolder,
                                             @Value("${patientRegistrationHeaders}")String headers) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "patientRegistration", headers);
    }

}
