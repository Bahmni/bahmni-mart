package org.bahmni.mart.form2;

import com.mysql.jdbc.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form2.model.Control;
import org.bahmni.mart.form2.model.ControlLabel;
import org.bahmni.mart.form2.model.ControlProperties;
import org.bahmni.mart.form2.model.Form2JsonMetadata;
import org.bahmni.mart.form2.uitl.Form2MetadataReader;
import org.bahmni.mart.helper.FormListHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Form2ListProcessor {

    @Autowired
    private Form2MetadataReader form2MetadataReader;
    private List<String> ignoreConceptNames;

    public List<BahmniForm> getAllForms(Map<String, String> allLatestFormPaths, JobDefinition jobDefinition) {
        ignoreConceptNames = JobDefinitionUtil.getIgnoreConceptNamesForJob(jobDefinition);
        ArrayList<BahmniForm> allBahmniForms = allLatestFormPaths.keySet().stream().map(formName ->
                getBahmniForm(formName, allLatestFormPaths.get(formName)))
                .collect(Collectors.toCollection(ArrayList::new));
        return FormListHelper.filterFormsWithOutDuplicateConcepts(allBahmniForms);
    }

    private BahmniForm getBahmniForm(String formName, String formJsonPath) {
        BahmniForm bahmniForm = new BahmniForm();
        Concept concept = createConcept(formName);
        bahmniForm.setFormName(concept);
        bahmniForm.setDepthToParent(0);
        Form2JsonMetadata form2JsonMetadata = form2MetadataReader.read(formJsonPath);
        form2JsonMetadata.getControls().forEach(control -> {
            parseControl(control, bahmniForm);
        });
        return bahmniForm;
    }

    private Concept createConcept(String formName) {
        Concept concept = new Concept();
        concept.setName(formName);
        return concept;
    }


    private void parseControl(Control control, BahmniForm bahmniForm) {
        String conceptName = getConceptName(control);
        if(!StringUtils.isNullOrEmpty(conceptName)){
            Concept concept = createConcept(conceptName);
            if(isNewFormRequiredByControl(control.getProperties())){
                BahmniForm childBahmniForm = createChildBahmniForm(concept);
                childBahmniForm.setParent(bahmniForm);
                bahmniForm.addChild(childBahmniForm);
                if (CollectionUtils.isNotEmpty(control.getControls()))
                    parseChildControls(control.getControls(), childBahmniForm);
            }else {
                if (CollectionUtils.isNotEmpty(control.getControls()))
                    parseChildControls(control.getControls(), bahmniForm);
                else
                    bahmniForm.addField(concept);
            }
        }
    }

    private void parseChildControls(List<Control> controls, BahmniForm bahmniForm) {
        controls.forEach(childControl -> parseControl(childControl, bahmniForm));
    }

    private String getConceptName(Control control) {
        String conceptName = "";
        if (CollectionUtils.isEmpty(control.getControls()) && control.getConcept() != null)
            conceptName = control.getConcept().getName();
        else {
            final ControlLabel controlLabel = control.getLabel();
            if (controlLabel != null)
                conceptName = controlLabel.getValue();
        }
        if (ignoreConceptNames.contains(conceptName))
            return "";
        return conceptName;
    }

    private BahmniForm createChildBahmniForm(Concept concept) {
        BahmniForm childBahmniForm = new BahmniForm();
        childBahmniForm.setFormName(concept);
       return childBahmniForm;
    }

    private boolean isNewFormRequiredByControl(ControlProperties properties) {
        return properties.isAddMore() || properties.isMultiSelect();
    }
}
