package org.bahmni.batch.exports;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class OtExportStep extends  BaseExportStep{


    @Autowired
    public OtExportStep(StepBuilderFactory stepBuilderFactory,
                                   DataSource dataSource,
                                   @Value("classpath:sql/otExport.sql") Resource sqlResource,
                                   @Value("${outputFolder}/otExport.csv") Resource outputFolder,
                                   @Value("${otExportHeaders}")String headers) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "otExport", headers);
    }

}