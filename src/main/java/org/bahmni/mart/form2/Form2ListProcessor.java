package org.bahmni.mart.form2;

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

import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Component
public class Form2ListProcessor {

    @Autowired
    private Form2MetadataReader form2MetadataReader;

    private List<String> ignoreConceptNames;
    private JobDefinition jobDefinition;

    public List<BahmniForm> getAllForms(Map<String, String> allLatestFormPaths, JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
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
            parseControl(control, bahmniForm, 0, false);
        });
        return bahmniForm;
    }

    private Concept createConcept(String formName) {
        Concept concept = new Concept();
        concept.setName(formName);
        return concept;
    }

    private Concept createConcept(Control control, String rootformName) {
        String conceptName = "";
        Concept concept = null;
        final org.bahmni.mart.form2.model.Concept form2Concept = control.getConcept();
        if (isEmpty(control.getControls()) && form2Concept != null) {
            conceptName = form2Concept.getName();
            if (ignoreConceptNames.contains(conceptName))
                return null;
            concept = new Concept();
            concept.setName(conceptName);
            concept.setDataType(form2Concept.getDatatype());
        } else {
            final ControlLabel controlLabel = control.getLabel();
            if (controlLabel != null) {
                conceptName = controlLabel.getValue();
                concept = new Concept();
                concept.setName(conceptName);
            }
        }
        if (control.getType() != null && control.getType().toLowerCase().equals("section"))
            concept.setName(rootformName + " " + concept.getName());
        return concept;
    }

    private void parseControl(Control control, BahmniForm bahmniForm, int depthToParent, boolean isParentAddMore) {
        Concept concept = createConcept(control, getRootForm(bahmniForm).getFormName().getName());
        if (isNull(concept)) {
            return;
        }
        if (isParentAddMore) {
            depthToParent++;
        }
        if (isNewFormRequiredByControl(control.getProperties())) {
            BahmniForm childBahmniForm = createChildBahmniForm(concept);
            childBahmniForm.setParent(bahmniForm);
            childBahmniForm.setRootForm(getRootForm(bahmniForm));
            childBahmniForm.setIsMultiSelect(control.getProperties().isMultiSelect());
            if (!isParentAddMore) {
                depthToParent++;
            }
            childBahmniForm.setDepthToParent(depthToParent);
            bahmniForm.addChild(childBahmniForm);
            processInnerControls(control, childBahmniForm, concept, depthToParent, true);
        } else {
            processInnerControls(control, bahmniForm, concept, depthToParent, isParentAddMore);
        }
    }

    private BahmniForm getRootForm(BahmniForm bahmniForm) {
        return bahmniForm.getDepthToParent() == 0 ? bahmniForm : bahmniForm.getRootForm();
    }

    private void processInnerControls(Control control, BahmniForm bahmniForm, Concept concept, int depthToParent,
                                      boolean isParentAddMore) {
        if (isNotEmpty(control.getControls())) {
            parseChildControls(control.getControls(), bahmniForm, depthToParent, isParentAddMore);
        } else {
            bahmniForm.addField(concept);
        }
    }

    private void parseChildControls(List<Control> controls, BahmniForm bahmniForm, int depthToParent,
                                    boolean isParentAddMore) {
        controls.forEach(childControl -> parseControl(childControl, bahmniForm, depthToParent, isParentAddMore));
    }

    private BahmniForm createChildBahmniForm(Concept concept) {
        BahmniForm childBahmniForm = new BahmniForm();
        childBahmniForm.setFormName(concept);
        return childBahmniForm;
    }

    private boolean isNewFormRequiredByControl(ControlProperties properties) {
        return JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition) &&
                (properties.isAddMore() || properties.isMultiSelect());
    }
}
