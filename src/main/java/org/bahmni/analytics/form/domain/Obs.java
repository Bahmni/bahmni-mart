package org.bahmni.analytics.form.domain;

public class Obs {
    private String treatmentNumber;
    private Integer id;
    private Integer parentId;
    private Concept field;
    private String value;

    public Obs() {
    }

    public Obs(String treatmentNumber, Integer id, Integer parentId, Concept field, String value) {
        this.treatmentNumber = treatmentNumber;
        this.id = id;
        this.parentId = parentId;
        this.field = field;
        this.value = value;
    }

    public String getTreatmentNumber() {
        return treatmentNumber;
    }

    public void setTreatmentNumber(String treatmentNumber) {
        this.treatmentNumber = treatmentNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Concept getField() {
        return field;
    }

    public void setField(Concept field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
