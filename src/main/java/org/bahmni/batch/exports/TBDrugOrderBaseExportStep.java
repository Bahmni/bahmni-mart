package org.bahmni.batch.exports;


import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
@Component
public class TBDrugOrderBaseExportStep extends  BaseExportStep{


    @Autowired
    public TBDrugOrderBaseExportStep(StepBuilderFactory stepBuilderFactory,
                                     DataSource dataSource,
                                     @Value("classpath:sql/tbDrugOrder.sql") Resource sqlResource,
                                     @Value("${outputFolder}/tbDrugOrder.csv") Resource outputFolder,
                                     @Value("${tbDrugOrderHeaders}")String headers) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "tbDrugOrder", headers);
    }


}
