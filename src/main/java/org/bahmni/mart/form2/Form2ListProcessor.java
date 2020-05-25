package org.bahmni.mart.form2;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form2.model.Control;
import org.bahmni.mart.form2.model.ControlLabel;
import org.bahmni.mart.form2.model.ControlProperties;
import org.bahmni.mart.form2.model.Form2JsonMetadata;
import org.bahmni.mart.form2.service.FormService;
import org.bahmni.mart.form2.translations.model.Form2Translation;
import org.bahmni.mart.form2.translations.util.Form2TranslationsReader;
import org.bahmni.mart.form2.uitl.Form2MetadataReader;
import org.bahmni.mart.helper.FormListHelper;
import org.bahmni.mart.helper.IgnoreColumnsConfigHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Component
public class Form2ListProcessor {

    private static final String DEFAULT_LOCALE = "en";
    Map<String, Integer> formNamesWithLatestVersionNumber;
    private HashSet<Concept> ignoreConcepts;
    private JobDefinition jobDefinition;
    @Autowired
    private Form2MetadataReader form2MetadataReader;
    @Autowired
    private IgnoreColumnsConfigHelper ignoreColumnsConfigHelper;
    @Autowired
    private FormService formService;

    private Form2TranslationsReader form2TranslationsReader;
    private Map<String, Form2Translation> formToTranslationsMap = new HashMap<>();
    private static final String CODED_DATA_TYPE = "Coded";

    @Autowired
    public Form2ListProcessor(FormService formService, Form2TranslationsReader form2TranslationsReader) {
        formNamesWithLatestVersionNumber = formService.getFormNamesWithLatestVersionNumber();
        this.form2TranslationsReader = form2TranslationsReader;
    }

    public List<BahmniForm> getAllForms(Map<String, String> allLatestFormPaths, JobDefinition jobDefinition, Map<String, String> formNameTranslationsMap) {
        this.jobDefinition = jobDefinition;
        ignoreConcepts = ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition);
        ArrayList<BahmniForm> allBahmniForms = allLatestFormPaths.keySet().stream().map(formName ->
                getBahmniForm(formName, allLatestFormPaths.get(formName), formNameTranslationsMap.get(formName)))
                .collect(Collectors.toCollection(ArrayList::new));
        return FormListHelper.filterFormsWithOutDuplicateSectionsAndConcepts(allBahmniForms);
    }

    private BahmniForm getBahmniForm(String formName, String formJsonPath, String translatedFormName) {
        BahmniForm bahmniForm = new BahmniForm();
        Concept concept = createConcept(formName);
        bahmniForm.setFormName(concept);
        bahmniForm.setTranslatedFormName(translatedFormName);
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

    private Concept createConcept(Control control, BahmniForm bahmniForm) {
        String rootformName = getRootForm(bahmniForm).getFormName().getName();
        String translatedFormName = getRootForm(bahmniForm).getTranslatedFormName() != null ? getRootForm(bahmniForm).getTranslatedFormName() : rootformName;
        String conceptName = "";
        Concept concept = null;
        final org.bahmni.mart.form2.model.Concept form2Concept = control.getConcept();
        if (isEmpty(control.getControls()) && form2Concept != null) {
            if (isInIgnoreConcepts(form2Concept.getName()))
                return null;
            String translatedConceptName = getTranslatedName(rootformName, control.getLabel().getTranslationKey());
            conceptName = isNull(translatedConceptName) ? form2Concept.getName() : translatedConceptName;
            concept = new Concept();
            concept.setName(conceptName);
            concept.setDataType(form2Concept.getDatatype());
        } else {
            final ControlLabel controlLabel = control.getLabel();
            if (controlLabel != null) {
                String translatedName = getTranslatedName(rootformName, controlLabel.getTranslationKey());
                concept = new Concept();
                concept.setName(isNull(translatedName) ? controlLabel.getValue() : translatedName);
            }
        }
        if (control.getType() != null && control.getType().toLowerCase().equals("section"))
            concept.setName(translatedFormName + " " + concept.getName());
        return concept;
    }

    private String getConceptNameFromFormJson(Control control) {
        String conceptName = "";
        final org.bahmni.mart.form2.model.Concept form2Concept = control.getConcept();
        if (isEmpty(control.getControls()) && form2Concept != null) {
            conceptName = form2Concept.getName();
            return conceptName;
        } else {
            return conceptName;
        }
    }

    private String getTranslatedName(String rootFormName, String translationKey) {
        if (!formToTranslationsMap.containsKey(rootFormName)) {
            formToTranslationsMap.put(rootFormName, getForm2Translation(rootFormName));
        }
        Form2Translation form2Translation = formToTranslationsMap.get(rootFormName);
        return form2TranslationsReader.getTranslation(form2Translation, translationKey);
    }

    private Form2Translation getForm2Translation(String rootFormName) {
        Integer formVersion = formNamesWithLatestVersionNumber.get(rootFormName);
        if (formVersion == null) {
            formVersion = 0;
        }
        String locale = jobDefinition.getLocale();
        locale = StringUtils.isBlank(locale) ? DEFAULT_LOCALE : locale;

        return form2TranslationsReader.read(rootFormName, formVersion, locale);
    }

    private boolean isInIgnoreConcepts(String conceptName) {
        return this.ignoreConcepts.stream().anyMatch(o -> o.getName().equals(conceptName));
    }

    private void parseControl(Control control, BahmniForm bahmniForm, int depthToParent, boolean isParentAddMore) {
        Concept concept = createConcept(control, bahmniForm);
        String conceptFullySpecifiedName = getConceptNameFromFormJson(control);
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
            processInnerControls(control, childBahmniForm, concept, depthToParent, true,
                    conceptFullySpecifiedName);
        } else {
            processInnerControls(control, bahmniForm, concept, depthToParent, isParentAddMore,
                    conceptFullySpecifiedName);
        }
    }

    private BahmniForm getRootForm(BahmniForm bahmniForm) {
        return bahmniForm.getDepthToParent() == 0 ? bahmniForm : bahmniForm.getRootForm();
    }

    private void processInnerControls(Control control, BahmniForm bahmniForm, Concept concept, int depthToParent,
                                      boolean isParentAddMore, String fullySpecifiedName) {
        if (isNotEmpty(control.getControls())) {
            parseChildControls(control.getControls(), bahmniForm, depthToParent, isParentAddMore);
        } else {
            bahmniForm.addField(concept);
            if (CODED_DATA_TYPE.equalsIgnoreCase(concept.getDataType())) {
                addCodedAnswersToBahmniFormMap(bahmniForm, control);
            }
            bahmniForm.addFieldNameAndFullySpecifiedNameMap(fullySpecifiedName, concept.getName());
        }
    }

    private void addCodedAnswersToBahmniFormMap(BahmniForm bahmniForm, Control control) {
        String rootFormName = bahmniForm.getRootForm() == null ? bahmniForm.getFormName().getName()
                : bahmniForm.getRootForm().getFormName().getName();
        control.getConcept().getAnswers()
                .stream()
                .forEach(conceptAnswer -> {
                    String translatedConceptAnswer = getTranslatedName(rootFormName, conceptAnswer.getTranslationKey());
                    bahmniForm.addFieldNameAndFullySpecifiedNameMap(conceptAnswer.getDisplayString(),
                            isNull(translatedConceptAnswer) ?
                                    conceptAnswer.getDisplayString() : translatedConceptAnswer);
                });
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
