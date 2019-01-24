package org.bahmni.mart.helper;

import org.bahmni.mart.table.TableDataExtractor;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class TableDataGeneratorTest {

    @Mock
    private JdbcTemplate openmrsJdbcTemplate;

    @Mock
    private JdbcTemplate martJdbcTemplate;

    private TableDataGenerator tableDataGenerator;

    private static final String LIMIT = " LIMIT 1";

    @Before
    public void setUp() throws Exception {
        tableDataGenerator = new TableDataGenerator();
        setValuesForMemberFields(tableDataGenerator, "openmrsJdbcTemplate", openmrsJdbcTemplate);
        setValuesForMemberFields(tableDataGenerator, "martJdbcTemplate", martJdbcTemplate);
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

        TableData actualTableData = tableDataGenerator.getTableDataFromOpenmrs(expectedTableName, sql);

        assertEquals(tableData, actualTableData);
        assertEquals(expectedTableName, actualTableData.getName());
        assertEquals("text", actualTableData.getColumns().get(0).getType());
        verify(openmrsJdbcTemplate, times(1)).query(eq(sql + LIMIT),
                any(TableDataExtractor.class));
    }

    @Test
    public void shouldReturnTableDataFromMartDatabase() {
        String sql = "some sql";
        String expectedTableName = "table_name";
        TableData tableData = new TableData();
        TableColumn tableColumn = new TableColumn("name", "varchar", false, null);
        tableData.setColumns(Collections.singletonList(tableColumn));

        when(martJdbcTemplate.query(eq(sql + LIMIT), any(TableDataExtractor.class))).thenReturn(tableData);

        TableData actualTableData = tableDataGenerator.getTableDataFromMart(expectedTableName, sql);

        verify(martJdbcTemplate).query(eq(sql + LIMIT),
                any(TableDataExtractor.class));
        assertTrue(tableData.equals(actualTableData));
    }

    @Test(expected = BadSqlGrammarException.class)
    public void shouldThrowBadSqlGrammarExceptionWhenTableIsNotPresentInMartDataBase() {
        when(martJdbcTemplate.query(anyString(), any(TableDataExtractor.class))).thenThrow(BadSqlGrammarException
                .class);

        tableDataGenerator.getTableDataFromMart("tablename","some sql");
    }
}
