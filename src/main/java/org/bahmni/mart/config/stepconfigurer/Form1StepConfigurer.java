package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class Form1StepConfigurer extends StepConfigurer {

    @Autowired
    protected FormListProcessor formListProcessor;

    @Autowired
    public Form1StepConfigurer(FormTableMetadataGenerator formTableMetadataGenerator){
        super(formTableMetadataGenerator);
    }

}
