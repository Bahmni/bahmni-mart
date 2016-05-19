package org.bahmni.batch.form.domain;

import org.bahmni.batch.observation.domain.Concept;

import java.util.ArrayList;
import java.util.List;

public class BahmniForm {

	private List<BahmniForm> children = new ArrayList<>();

	private BahmniForm parent;

	private Concept formName;

	private List<Concept> fields = new ArrayList<>();

	private int depthToParent;

	public List<BahmniForm> getChildren() {
		return children;
	}

	public void addChild(BahmniForm bahmniForm){
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

	public void addField(Concept concept){
		fields.add(concept);
	}

	public int getDepthToParent() {
		return depthToParent;
	}

	public void setDepthToParent(int depthToParent) {
		this.depthToParent = depthToParent;
	}

	public String getDisplayName() {
		if(formName == null)
			return "";
		return formName.getName().replaceAll("\\s", "") ;
	}

}
