package org.bahmni.mart.config;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.view.ViewDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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
        PowerMockito.mockStatic(BatchUtils.class);
        when(BatchUtils.convertResourceOutputToString(any())).thenReturn(json);

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
}