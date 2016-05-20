package org.bahmni.batch.exports;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class MetaDataCodeDictionaryExportStep extends BaseExportStep {
    @Autowired
    public MetaDataCodeDictionaryExportStep(StepBuilderFactory stepBuilderFactory,
                                            DataSource dataSource,
                                            @Value("classpath:sql/metaDataCodeDictionary.sql") Resource sqlResource,
                                            @Value("${outputFolder}/metaDataCodeDictionary.csv") Resource outputFolder,
                                            @Value("${metaDataCodeDictionaryHeaders}") String headers) {
        super(stepBuilderFactory, dataSource, sqlResource, outputFolder, "metaDataCodeDictionary", headers);
    }

}
