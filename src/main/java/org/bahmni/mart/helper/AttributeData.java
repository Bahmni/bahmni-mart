package org.bahmni.mart.helper;

public enum AttributeData {
    person_attribute_type("format", "org.openmrs.Concept"),
    surgical_appointment_attribute_type("format", "org.openmrs.Concept"),
    visit_attribute_type("datatype", "org.openmrs.Concept"),
    provider_attribute_type("datatype", "org.openmrs.Concept"),
    patient_identifier_type("format", "org.openmrs.Concept"),
    program_attribute_type("datatype", "org.bahmni.module.bahmnicore.customdatatype.datatype.CodedConceptDatatype");

    private String columnName;
    private String dataType;


    AttributeData(String columnName, String dataType) {
        this.columnName = columnName;
        this.dataType = dataType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getDataType() {
        return dataType;
    }
}
