package org.bahmni.mart.helper;

public enum AttributeColumnName {
    person_attribute_type("format"),
    surgical_appointment_attribute_type("format"),
    visit_attribute_type("datatype"),
    provider_attribute_type("datatype"),
    patient_identifier_type("format");

    private String datatype;

    AttributeColumnName(String datatype) {
        this.datatype = datatype;
    }

    public String getDatatype() {
        return datatype;
    }
}
