package org.bahmni.mart.helper;

import org.bahmni.mart.table.TableDataExtractor;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class TableDataGeneratorTest {

    @Mock
    private JdbcTemplate openmrsJdbcTemplate;

    private TableDataGenerator tableDataGenerator;

    private static final String LIMIT = " LIMIT 1";

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        tableDataGenerator = new TableDataGenerator();
        setValuesForMemberFields(tableDataGenerator, "openmrsJdbcTemplate", openmrsJdbcTemplate);
        setValueForFinalStaticField(TableDataGenerator.class, "LIMIT", LIMIT);
    }

    @Test
    public void shouldReturnTableData() {
        String sql = "some sql";
        String expectedTableName = "radiology";
        TableData tableData = new TableData();
        TableColumn tableColumn = new TableColumn("name", "varchar", false, null);
        tableData.setColumns(Collections.singletonList(tableColumn));

        when(openmrsJdbcTemplate.query(eq(sql + LIMIT), any(TableDataExtractor.class))).thenReturn(tableData);

        TableData actualTableData = tableDataGenerator.getTableData(expectedTableName, sql);

        Assert.assertEquals(tableData, actualTableData);
        Assert.assertEquals(expectedTableName, actualTableData.getName());
        Assert.assertEquals("text", actualTableData.getColumns().get(0).getType());
        verify(openmrsJdbcTemplate, times(1)).query(eq(sql + LIMIT),
                any(TableDataExtractor.class));
    }
}