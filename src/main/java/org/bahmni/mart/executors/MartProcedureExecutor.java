package org.bahmni.mart.executors;

import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.procedure.ProcedureDefinition;
import org.bahmni.mart.config.procedure.ProcedureExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MartProcedureExecutor implements MartExecutor {

    @Autowired
    private ProcedureExecutor procedureExecutor;

    @Autowired
    private MartJSONReader martJSONReader;

    @Override
    public void execute() {
        List<ProcedureDefinition> procedures = martJSONReader.getProcedureDefinitions();
        procedureExecutor.execute(procedures);
    }
}
