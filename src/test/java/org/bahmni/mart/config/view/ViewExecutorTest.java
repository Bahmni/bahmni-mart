package org.bahmni.mart.config.view;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
public class ViewExecutorTest {

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Rule
    private ExpectedException expectedException = ExpectedException.none();
    private ViewExecutor viewExecutor;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        viewExecutor = new ViewExecutor();
        setValuesForMemberFields(viewExecutor, "martJdbcTemplate", martJdbcTemplate);
    }

    @Test
    public void shouldExecuteTwoViewsSuccessfully() {
        ViewDefinition viewDefinition = new ViewDefinition();
        viewDefinition.setName("view1");
        viewDefinition.setViewSQL("create view patient_view as select * from patient");

        ViewDefinition viewDefinition1 = new ViewDefinition();
        viewDefinition1.setName("view2");
        viewDefinition1.setViewSQL("create view program_view as select * from program");

        viewExecutor.execute(Arrays.asList(viewDefinition, viewDefinition1));

        verify(martJdbcTemplate, times(1))
                .execute("drop view if exists patient_view;" + viewDefinition.getViewSQL());
        verify(martJdbcTemplate, times(1))
                .execute("drop view if exists program_view;" + viewDefinition1.getViewSQL());
    }

    @Test
    public void shouldThrowExceptionIfViewSQLIsNotProper() throws NoSuchFieldException, IllegalAccessException {
        ViewDefinition viewDefinition = new ViewDefinition();
        viewDefinition.setName("view1");
        viewDefinition.setViewSQL("create view patient_view as select * from patient");

        doThrow(Exception.class).when(martJdbcTemplate).execute(anyString());
        Logger logger = mock(Logger.class);
        setValuesForMemberFields(viewExecutor, "logger", logger);

        viewExecutor.execute(Arrays.asList(viewDefinition));
        verify(logger, times(1)).error(eq("Unable to execute the view view1."),
                any(Exception.class));
    }
}