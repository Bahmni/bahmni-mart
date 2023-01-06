package org.bahmni.mart.config.view;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.SQLFileLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.config.job.SQLFileLoader.loadResource;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({BatchUtils.class, SQLFileLoader.class})
@RunWith(PowerMockRunner.class)
public class ViewExecutorTest {

    @Mock
    private JdbcTemplate martJdbcTemplate;

    private ViewExecutor viewExecutor;

    @Before
    public void setUp() throws Exception {
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
    public void shouldLogTheErrorIfViewSQLSyntaxIsIncorrect() throws Exception {
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
    public void shouldNotExecuteTheViewIfViewSQLIsEmpty() {
        ViewDefinition viewDefinition = new ViewDefinition();
        viewDefinition.setName("view1");
        viewDefinition.setSql("");

        viewExecutor.execute(Arrays.asList(viewDefinition));

        verify(martJdbcTemplate, times(0)).execute(anyString());
    }

    @Test
    public void shouldReadSqlFromFileGivenEmptySql() throws Exception {
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        when(viewDefinition.getSql()).thenReturn(null);
        when(viewDefinition.getName()).thenReturn("view_from_file");
        String sourceFilePath = "some path";
        when(viewDefinition.getSourceFilePath()).thenReturn(sourceFilePath);
        mockStatic(SQLFileLoader.class);
        Resource resource = mock(Resource.class);
        when(loadResource(sourceFilePath)).thenReturn(resource);
        mockStatic(BatchUtils.class);
        String sqlFromFile = "sql from file";
        when(BatchUtils.convertResourceOutputToString(resource)).thenReturn(sqlFromFile);

        viewExecutor.execute(Arrays.asList(viewDefinition));

        verify(viewDefinition, times(2)).getSourceFilePath();
        verifyStatic(times(1));
        loadResource(sourceFilePath);
        verifyStatic(times(1));
        BatchUtils.convertResourceOutputToString(resource);
        verify(martJdbcTemplate, times(1)).execute("drop view if exists view_from_file;" +
                "create view view_from_file as " + sqlFromFile);
    }

    @Test
    public void shouldNotExecuteEmptySqlAsViewWhenSqlAndSourceFilePathAreEmpty() {
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        when(viewDefinition.getName()).thenReturn("invalid_view");

        viewExecutor.execute(Arrays.asList(viewDefinition));

        verify(martJdbcTemplate, times(0)).execute(anyString());
    }

    @Test
    public void shouldGiveFailedViews() {
        ViewDefinition viewDefinitionOne = new ViewDefinition();
        viewDefinitionOne.setName("view1");
        viewDefinitionOne.setSql("select * from patient");

        ViewDefinition viewDefinitionTwo = new ViewDefinition();
        viewDefinitionTwo.setName("view2");
        viewDefinitionTwo.setSql("select * from program");

        doThrow(Exception.class).when(martJdbcTemplate).execute("drop view if exists view1;create view view1 as " +
                viewDefinitionOne.getSql());

        viewExecutor.execute(Arrays.asList(viewDefinitionOne, viewDefinitionTwo));
        List<String> actualFailedViews = viewExecutor.getFailedViews();

        List<String> expectedFailedViews = Collections.singletonList("view1");
        assertTrue(expectedFailedViews.containsAll(actualFailedViews));

    }
}