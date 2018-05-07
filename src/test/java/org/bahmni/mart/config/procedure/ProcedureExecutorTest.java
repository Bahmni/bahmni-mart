package org.bahmni.mart.config.procedure;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.SQLFileLoader;
import org.bahmni.mart.exception.BatchResourceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.Arrays;

import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.config.job.SQLFileLoader.loadResource;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({BatchUtils.class, SQLFileLoader.class})
@RunWith(PowerMockRunner.class)
public class ProcedureExecutorTest {

    private ProcedureExecutor procedureExecutor;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private Resource resource;

    @Mock
    private Logger logger;

    @Before
    public void setUp() throws Exception {
        procedureExecutor = new ProcedureExecutor();
        setValuesForMemberFields(procedureExecutor, "martJdbcTemplate", jdbcTemplate);
        setValuesForMemberFields(procedureExecutor, "logger", logger);
        mockStatic(SQLFileLoader.class);
        mockStatic(BatchUtils.class);
    }

    @Test
    public void shouldExecuteProcedureFromGivenSqlFilePath() throws Exception {
        ProcedureDefinition procedureDefinition = mock(ProcedureDefinition.class);
        when(procedureDefinition.getName()).thenReturn("Test Procedure");
        String path = "Some path";
        when(procedureDefinition.getSqlFilePath()).thenReturn(path);
        when(loadResource(path)).thenReturn(resource);
        String validSql = "valid sql";
        when(convertResourceOutputToString(resource)).thenReturn(validSql);

        procedureExecutor.execute(Arrays.asList(procedureDefinition));

        verify(jdbcTemplate, times(1)).execute(validSql);
    }

    @Test
    public void shouldLogErrorGivenWrongFilePath() throws Exception {
        ProcedureDefinition procedureDefinition = mock(ProcedureDefinition.class);
        when(procedureDefinition.getName()).thenReturn("Test Procedure");
        String path = "invalid path";
        when(procedureDefinition.getSqlFilePath()).thenReturn(path);
        when(loadResource(path)).thenThrow(BatchResourceException.class);

        procedureExecutor.execute(Arrays.asList(procedureDefinition));

        verify(logger, times(1)).error(eq("Unable to execute the procedure Test Procedure"),
                any(BatchResourceException.class));
    }

    @Test
    public void shouldLogErrorWhenSqlHasDropToken() throws Exception {
        ProcedureDefinition procedureDefinition = mock(ProcedureDefinition.class);
        when(procedureDefinition.getName()).thenReturn("Test Procedure");
        String path = "invalid path";
        when(procedureDefinition.getSqlFilePath()).thenReturn(path);
        when(loadResource(path)).thenReturn(resource);
        when(convertResourceOutputToString(resource)).thenReturn("DROP sql");

        procedureExecutor.execute(Arrays.asList(procedureDefinition));

        verify(logger, times(1)).error(eq("Unable to execute the procedure Test Procedure"),
                any(SQLException.class));
    }

    @Test
    public void shouldExecuteEmptySqlGivenEmptyOrNullSqlFilePath() throws Exception {
        ProcedureDefinition procedureDefinition = mock(ProcedureDefinition.class);
        when(procedureDefinition.getName()).thenReturn("Test Procedure");

        procedureExecutor.execute(Arrays.asList(procedureDefinition));

        verify(jdbcTemplate, times(1)).execute("");
    }
}