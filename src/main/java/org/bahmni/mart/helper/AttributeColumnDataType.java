package org.bahmni.mart.helper;

public enum AttributeColumnDataType {
    person_attribute_type("org.openmrs.Concept"),
    surgical_appointment_attribute_type("org.openmrs.Concept"),
    visit_attribute_type("org.openmrs.Concept"),
    provider_attribute_type("org.openmrs.Concept"),
    patient_identifier_type("org.openmrs.Concept"),
    program_attribute_type("org.bahmni.module.bahmnicore.customdatatype.datatype.CodedConceptDatatype");

    private String dataTypeValue;

    AttributeColumnDataType(String dataTypeValue) {
        this.dataTypeValue = dataTypeValue;
    }

    public String getDataTypeValue() {
        return dataTypeValue;
    }
}
