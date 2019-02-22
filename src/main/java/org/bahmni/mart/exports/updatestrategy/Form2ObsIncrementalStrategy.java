package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.table.Form2TableMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Qualifier("form2ObsIncrementalStrategy")
public class Form2ObsIncrementalStrategy extends ObsIncrementalUpdateStrategy {

    @Autowired
    private Form2TableMetadataGenerator form2TableMetadataGenerator;

    @PostConstruct
    public void postConstruct() {
        super.setTableMetadataGenerator(form2TableMetadataGenerator);
    }
}
