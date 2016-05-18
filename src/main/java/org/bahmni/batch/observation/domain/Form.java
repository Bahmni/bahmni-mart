package org.bahmni.batch.observation.domain;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Form {
	private Concept formName;

	private Concept parentConcept;

	private int depthToParent;

	private List<Form> children;
	private List<Concept> fields = new ArrayList<>();

	private int depthFromParent = 4;

	private List<Concept> ignoredFields = new ArrayList<>();

	public void setFormName(Concept formName) {
		this.formName = formName;
	}

	public Concept getFormName() {
		return formName;
	}

	public List<Concept> getFields() {
		return fields;
	}

	public List<Concept> getIgnoredFields() {
		return ignoredFields;
	}

	public void addIgnoreField(Concept conceptToIgnore){
		ignoredFields.add(conceptToIgnore);
	}

	public void addField(Concept concept){
		fields.add(concept);
	}

	public int getDepthFromParent() {
		return depthFromParent;
	}
	public void setDepthFromParent(int depthFromParent) {
		this.depthFromParent = depthFromParent;
	}
}
