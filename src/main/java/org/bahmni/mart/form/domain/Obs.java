package org.bahmni.mart.form.domain;

public class Obs {
    private String encounterId;
    private String patientId;
    private Integer id;
    private Integer parentId;
    private Concept field;
    private String value;
    private String parentName;
    private String obsDateTime;
    private String dateCreated;
    private String locationId;
    private String locationName;
    private String programId;
    private String programName;
    private String formFieldPath;
    private String referenceFormFieldPath;

    public Obs() {
    }

    public Obs(Integer id, Integer parentId, Concept field, String value) {
        this.id = id;
        this.parentId = parentId;
        this.field = field;
        this.value = value;
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


    public String getEncounterId() {
        return encounterId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getObsDateTime() {
        return obsDateTime;
    }

    public void setObsDateTime(String obsDateTime) {
        this.obsDateTime = obsDateTime;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getFormFieldPath() {
        return formFieldPath;
    }

    public void setFormFieldPath(String formFieldPath) {
        this.formFieldPath = formFieldPath;
    }

    public String getReferenceFormFieldPath() {
        return referenceFormFieldPath;
    }

    public void setReferenceFormFieldPath(String referenceFormFieldPath) {
        this.referenceFormFieldPath = referenceFormFieldPath;
    }
}
