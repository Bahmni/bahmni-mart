package org.bahmni.batch.exports;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class PatientRegistrationBaseExportStep extends BaseExportStep {

    @Autowired
    public PatientRegistrationBaseExportStep(StepBuilderFactory stepBuilderFactory, DataSource dataSource,
                                             @Value("classpath:sql/patientInformation.sql") Resource sqlResource, @Value("${outputFolder}/patientInformation.csv") Resource outputFolder) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "patientInformation");
    }

}
