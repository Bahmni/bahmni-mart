package org.bahmni.mart.config.procedure;

public class ProcedureDefinition {

    private String name;
    private String sourceFilePath;
    private ProcedureParameters procedureParameters;

    public String getName() {
        return name;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    ProcedureParameters getProcedureParameters() {
        return procedureParameters;
    }
}
