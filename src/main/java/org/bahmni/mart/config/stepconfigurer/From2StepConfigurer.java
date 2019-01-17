package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form2.Form2ListProcessor;
import org.bahmni.mart.form2.service.FormService;
import org.bahmni.mart.helper.FormListHelper;
import org.bahmni.mart.table.Form2TableMetadataGenerator;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;

@Component
public class From2StepConfigurer extends StepConfigurer {

    private static final String TYPE = "form2Obs";

    @Autowired
    private FormService formService;

    @Autowired
    protected Form2ListProcessor form2ListProcessor;

    @Autowired
    public From2StepConfigurer(Form2TableMetadataGenerator formTableMetadataGenerator){
        super(formTableMetadataGenerator);
    }

    @Override
    protected List<BahmniForm> getAllForms() {
        JobDefinition jobDefinition = getJobDefinitionByType(jobDefinitionReader.getJobDefinitions(), TYPE);
        Map<String, String> allLatestFormPaths = formService.getAllLatestFormPaths();
        return FormListHelper.flattenFormList(form2ListProcessor.getAllForms(allLatestFormPaths, jobDefinition));
    }
}
