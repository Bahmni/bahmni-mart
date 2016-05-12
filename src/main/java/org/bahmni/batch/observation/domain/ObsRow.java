package org.bahmni.batch.observation.domain;


public class ObsRow {
    private int conceptId;
    private int obsId;
    private boolean isSet;
    private String value;
    private String conceptName;

    public ObsRow(){

    }

    public ObsRow(int conceptId, int obsId, boolean isSet, String value, String conceptName){
        this.conceptId=conceptId;
        this.obsId = obsId;
        this.isSet = isSet;
        this.value = value;
        this.conceptName = conceptName;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public boolean isSet() {
        return isSet;
    }

    public void setSet(boolean isSet) {
        this.isSet = isSet;
    }

    public int getObsId() {
        return obsId;
    }

    public void setObsId(int obsId) {
        this.obsId = obsId;
    }

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
