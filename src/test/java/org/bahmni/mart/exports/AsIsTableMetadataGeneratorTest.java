package org.bahmni.mart.exports;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Mockito.when;

@PrepareForTest(BatchUtils.class)
@RunWith(PowerMockRunner.class)
public class AsIsTableMetadataGeneratorTest {

    private AsIsTableMetadataGenerator asIsTableGenerator;

    @Mock
    private Resource metadataJson;

    private String json;

    @Before
    public void setUp() throws Exception {
        asIsTableGenerator = new AsIsTableMetadataGenerator();
        PowerMockito.mockStatic(BatchUtils.class);
        json = "[\n" +
                "  {\n" +
                "    \"name\": \"programs\",\n" +
                "    \"columns\": [\n" +
                "      {\n" +
                "        \"name\": \"Name\",\n" +
                "        \"type\": \"\",\n" +
                "        \"isPrimaryKey\": false,\n" +
                "        \"reference\": null\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"Id\",\n" +
                "        \"type\": \"\",\n" +
                "        \"isPrimaryKey\": false,\n" +
                "        \"reference\": null\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"episodes\",\n" +
                "    \"columns\": [\n" +
                "      {\n" +
                "        \"name\": \"Name1\",\n" +
                "        \"type\": \"\",\n" +
                "        \"isPrimaryKey\": false,\n" +
                "        \"reference\": null\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"Id1\",\n" +
                "        \"type\": \"\",\n" +
                "        \"isPrimaryKey\": false,\n" +
                "        \"reference\": null\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]\n";
        setValuesForMemberFields(asIsTableGenerator, "metadataJson", metadataJson);
    }

    @Test
    public void shouldSetupTableDataForGivenInfo() throws Exception {
        when(BatchUtils.convertResourceOutputToString(metadataJson)).thenReturn(json);

        List<TableData> tableDataList = asIsTableGenerator.getTables();
        Assert.assertEquals(2, tableDataList.size());

        Assert.assertEquals("Name", tableDataList.get(0).getColumns().get(0).getName());
        Assert.assertEquals("Id", tableDataList.get(0).getColumns().get(1).getName());

        Assert.assertEquals("Name1", tableDataList.get(1).getColumns().get(0).getName());
        Assert.assertEquals("Id1", tableDataList.get(1).getColumns().get(1).getName());
    }

}