package org.bahmni.mart.exports;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class AppointmentSchedulingExportStep extends BaseExportStep {
    @Autowired
    public AppointmentSchedulingExportStep(StepBuilderFactory stepBuilderFactory,
                                   DataSource dataSource,
                                   @Value("classpath:sql/appointmentSchedulingExport.sql") Resource sqlResource,
                                   @Value("${outputFolder}/appointmentSchedulingExport.csv") Resource outputFolder,
                                   @Value("${appointmentSchedulingExportHeaders}") String headers) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "appointmentSchedulingExport", headers);
    }

}