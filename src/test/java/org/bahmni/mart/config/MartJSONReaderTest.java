package org.bahmni.mart.config;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.view.ViewDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BatchUtils.class)
public class MartJSONReaderTest {

    private MartJSONReader martJSONReader;

    @Before
    public void setUp() {
        String json = "{" +
                "\"jobs\": [\n" +
                "  {\n" +
                "    \"name\": \"Program Data\",\n" +
                "    \"type\": \"generic\",\n" +
                "    \"readerSql\": \"select * from program\",\n" +
                "    \"chunkSizeToRead\": \"1000\",\n" +
                "    \"tableName\": \"MyProgram\"\n" +
                "  }\n" +
                "]," +
                "\"views\": [\n" +
                "  {\n" +
                "       \"name\": \"patient_program_view\",\n" +
                "       \"sql\": \"select * from patient_program\"\n" +
                "  }]\n" +
                "}";
        mockStatic(BatchUtils.class);
        when(convertResourceOutputToString(any())).thenReturn(json);

        martJSONReader = new MartJSONReader();
        martJSONReader.read();
    }

    @Test
    public void shouldReturnJobDefinitions() {
        List<JobDefinition> actualJobDefinitions = martJSONReader.getJobDefinitions();

        assertEquals(1, actualJobDefinitions.size());
        JobDefinition jobDefinition = actualJobDefinitions.get(0);
        assertEquals("Program Data", jobDefinition.getName());
        assertEquals("generic", jobDefinition.getType());
        assertEquals("select * from program", jobDefinition.getReaderSql());
        assertEquals(1000, jobDefinition.getChunkSizeToRead());
        assertEquals("MyProgram", jobDefinition.getTableName());
    }

    @Test
    public void shouldReturnViewDefinitions() {
        List<ViewDefinition> viewDefinitions = martJSONReader.getViewDefinitions();

        assertEquals(1, viewDefinitions.size());
        ViewDefinition viewDefinition = viewDefinitions.get(0);
        assertEquals("patient_program_view", viewDefinition.getName());
        assertEquals("select * from patient_program",
                viewDefinition.getSql());
    }

    @Test
    public void shouldGiveEmptyListAsJobDefinitionsIfJobsAreNotPresent() throws Exception {
        BahmniMartJSON bahmniMartJSON = new BahmniMartJSON();
        BahmniMartJSON spyMartJson = spy(bahmniMartJSON);
        setValuesForMemberFields(martJSONReader, "bahmniMartJSON", spyMartJson);

        assertTrue(martJSONReader.getJobDefinitions().isEmpty());
        verify(spyMartJson, times(1)).getJobs();
    }

    @Test
    public void shouldGiveEmptyListAsViewsIfViewsAreNotPresentInConfig() throws Exception {
        BahmniMartJSON bahmniMartJSON = new BahmniMartJSON();
        BahmniMartJSON spyMartJson = spy(bahmniMartJSON);

        setValuesForMemberFields(martJSONReader, "bahmniMartJSON", spyMartJson);

        assertTrue(martJSONReader.getViewDefinitions().isEmpty());
        verify(spyMartJson, times(1)).getViews();

    }
}