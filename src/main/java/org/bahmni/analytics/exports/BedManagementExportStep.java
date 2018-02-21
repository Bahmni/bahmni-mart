package org.bahmni.analytics.exports;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class BedManagementExportStep extends BaseExportStep {
    @Autowired
    public BedManagementExportStep(StepBuilderFactory stepBuilderFactory,
                                   DataSource dataSource,
                                   @Value("classpath:sql/bedManagementExport.sql") Resource sqlResource,
                                   @Value("${outputFolder}/bedManagementExport.csv") Resource outputFolder,
                                   @Value("${bedManagementExportHeaders}") String headers) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "bedManagementExport", headers);
    }

}
