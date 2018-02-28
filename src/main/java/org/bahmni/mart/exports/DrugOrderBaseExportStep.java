package org.bahmni.mart.exports;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DrugOrderBaseExportStep extends BaseExportStep {


    @Autowired
    public DrugOrderBaseExportStep(StepBuilderFactory stepBuilderFactory,
                                   DataSource dataSource,
                                   @Value("classpath:sql/drugOrder.sql") Resource sqlResource,
                                   @Value("${outputFolder}/drugOrder.csv") Resource outputFolder,
                                   @Value("${drugOrderHeaders}") String headers) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "drugOrder", headers);
    }

}
