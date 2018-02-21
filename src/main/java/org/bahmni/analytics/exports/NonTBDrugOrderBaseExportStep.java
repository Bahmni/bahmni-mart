package org.bahmni.analytics.exports;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class NonTBDrugOrderBaseExportStep extends BaseExportStep {

    @Autowired
    public NonTBDrugOrderBaseExportStep(StepBuilderFactory stepBuilderFactory,
                                        DataSource dataSource,
                                        @Value("classpath:sql/nonTbDrugOrder.sql") Resource sqlResource,
                                        @Value("${outputFolder}/nonTbDrugOrder.csv") Resource outputFolder,
                                        @Value("${nonTbDrugOrderHeaders}") String headers) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "nonTbDrugOrder", headers);
    }
}
