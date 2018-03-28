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
        viewDefinition.setSql("select * from patient");

        ViewDefinition viewDefinition1 = new ViewDefinition();
        viewDefinition1.setName("view2");
        viewDefinition1.setSql("select * from program");

        viewExecutor.execute(Arrays.asList(viewDefinition, viewDefinition1));

        verify(martJdbcTemplate, times(1))
                .execute("drop view if exists view1;create view view1 as " + viewDefinition.getSql());
        verify(martJdbcTemplate, times(1))
                .execute("drop view if exists view2;create view view2 as " + viewDefinition1.getSql());
    }

    @Test
    public void shouldLogTheErrorIfViewSQLSyntaxIsIncorrect() throws NoSuchFieldException, IllegalAccessException {
        ViewDefinition viewDefinition = new ViewDefinition();
        viewDefinition.setName("view1");
        viewDefinition.setSql("select * from patient");

        doThrow(Exception.class).when(martJdbcTemplate).execute(anyString());
        Logger logger = mock(Logger.class);
        setValuesForMemberFields(viewExecutor, "logger", logger);

        viewExecutor.execute(Arrays.asList(viewDefinition));
        verify(logger, times(1)).error(eq("Unable to execute the view view1."),
                any(Exception.class));
    }

    @Test
    public void shouldLogTheErrorIfViewSQLIsEmpty() throws NoSuchFieldException, IllegalAccessException {
        ViewDefinition viewDefinition = new ViewDefinition();
        viewDefinition.setName("view1");
        viewDefinition.setSql("");

        doThrow(Exception.class).when(martJdbcTemplate).execute("drop view if exists view1;create view view1 as ");
        Logger logger = mock(Logger.class);
        setValuesForMemberFields(viewExecutor, "logger", logger);

        viewExecutor.execute(Arrays.asList(viewDefinition));
        verify(logger, times(1)).error(eq("Unable to execute the view view1."),
                any(Exception.class));
    }


    @Test
    public void shouldLogTheErrorIfViewSQLIsNull() throws NoSuchFieldException, IllegalAccessException {
        ViewDefinition viewDefinition = new ViewDefinition();
        viewDefinition.setName("view1");
        viewDefinition.setSql(null);

        doThrow(Exception.class).when(martJdbcTemplate)
                .execute("drop view if exists view1;create view view1 as null");
        Logger logger = mock(Logger.class);
        setValuesForMemberFields(viewExecutor, "logger", logger);

        viewExecutor.execute(Arrays.asList(viewDefinition));
        verify(logger, times(1)).error(eq("Unable to execute the view view1."),
                any(Exception.class));
    }
}