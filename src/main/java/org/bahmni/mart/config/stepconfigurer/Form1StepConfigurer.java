package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Form1StepConfigurer extends StepConfigurer {

    @Autowired
    protected FormListProcessor formListProcessor;

    @Autowired
    public Form1StepConfigurer(FormTableMetadataGenerator formTableMetadataGenerator) {
        super(formTableMetadataGenerator);
    }
}
