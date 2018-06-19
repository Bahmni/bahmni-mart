package org.bahmni.mart.executors;

import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.procedure.ProcedureDefinition;
import org.bahmni.mart.config.procedure.ProcedureExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MartProcedureExecutorTest {

    private MartProcedureExecutor martProcedureExecutor;

    @Mock
    private ProcedureExecutor procedureExecutor;

    @Mock
    private MartJSONReader martJSONReader;

    @Mock
    private ProcedureDefinition procedureDefinition;

    @Before
    public void setUp() throws Exception {

        martProcedureExecutor = new MartProcedureExecutor();

        setValuesForMemberFields(martProcedureExecutor, "procedureExecutor", procedureExecutor);
        setValuesForMemberFields(martProcedureExecutor, "martJSONReader", martJSONReader);
    }

    @Test
    public void shouldExecuteProcedures() {
        List<ProcedureDefinition> procedureDefinitions = Collections.singletonList(procedureDefinition);
        when(martJSONReader.getProcedureDefinitions()).thenReturn(procedureDefinitions);

        martProcedureExecutor.execute();

        verify(procedureExecutor, times(1)).execute(procedureDefinitions);

    }
}