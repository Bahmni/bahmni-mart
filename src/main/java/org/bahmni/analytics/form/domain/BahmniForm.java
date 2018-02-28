package org.bahmni.analytics.form.domain;

import java.util.ArrayList;
import java.util.List;

public class BahmniForm {

    private List<BahmniForm> children = new ArrayList<>();

    private BahmniForm parent;

    private BahmniForm rootForm;

    private Concept formName;

    private List<Concept> fields = new ArrayList<>();

    private int depthToParent;

    public List<BahmniForm> getChildren() {
        return children;
    }

    public void addChild(BahmniForm bahmniForm) {
        children.add(bahmniForm);
    }

    public BahmniForm getParent() {
        return parent;
    }

    public void setParent(BahmniForm parent) {
        this.parent = parent;
    }

    public Concept getFormName() {
        return formName;
    }

    public void setFormName(Concept formName) {
        this.formName = formName;
    }

    public List<Concept> getFields() {
        return fields;
    }

    public void addField(Concept concept) {
        fields.add(concept);
    }

    public int getDepthToParent() {
        return depthToParent;
    }

    public void setDepthToParent(int depthToParent) {
        this.depthToParent = depthToParent;
    }

    public void setRootForm(BahmniForm rootForm) {
        this.rootForm = rootForm;
    }

    public BahmniForm getRootForm() {
        return rootForm;
    }

}
