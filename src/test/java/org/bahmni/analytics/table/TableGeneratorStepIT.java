package org.bahmni.analytics.table;

import org.bahmni.analytics.AbstractBaseBatchIT;
import org.bahmni.analytics.table.domain.ForeignKey;
import org.bahmni.analytics.table.domain.TableColumn;
import org.bahmni.analytics.table.domain.TableData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TableGeneratorStepIT extends AbstractBaseBatchIT {

    @Autowired
    TableGeneratorStep tableGeneratorStep;

    @Qualifier("postgresJdbcTemplate")
    @Autowired
    private JdbcTemplate postgresJdbcTemplate;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void shouldCreateTableWithTableData() {
        TableData tableData = new TableData("tablename");

        tableData.addColumn(new TableColumn("column_one", "Integer", false, null));
        tableData.addColumn(new TableColumn("column_two", "Integer", false, null));
        tableData.addColumn(new TableColumn("column_three", "Integer", false, null));

        tableGeneratorStep.createTables(Arrays.asList(tableData));
        postgresJdbcTemplate.queryForList("SELECT * FROM \"tablename\"");
        assertTrue(true);
    }

    @Test
    public void shouldCreateTableWithReference() {
        TableData tableData = new TableData("tablename");
        TableData referenceTableData = new TableData("foreignkeytable");

        referenceTableData.addColumn(new TableColumn("foreignkeycolumn", "Integer", true, null));

        tableData.addColumn(new TableColumn("column_one", "Integer", false, null));
        tableData.addColumn(new TableColumn("column_two", "Integer", false, null));
        ForeignKey foreignKey = new ForeignKey("foreignkeycolumn", "foreignkeytable");
        tableData.addColumn(new TableColumn("column_three", "Integer", false, foreignKey));

        tableGeneratorStep.createTables(Arrays.asList(referenceTableData, tableData));
        postgresJdbcTemplate.queryForList("SELECT * FROM \"tablename\"");
        List<Object> tableDataColumns = postgresJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tablename' AND TABLE_SCHEMA='PUBLIC';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());

        assertEquals(3, tableDataColumns.size());
        assertEquals(new HashSet<>(Arrays.asList("column_one", "column_two", "column_three")),
                new HashSet<>(tableDataColumns));


        postgresJdbcTemplate.queryForList("SELECT * FROM \"foreignkeytable\"");
        List<Object> referenceTableDataColumns = postgresJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'foreignkeytable' AND TABLE_SCHEMA='PUBLIC';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());

        assertEquals(1, referenceTableDataColumns.size());
        assertEquals(new HashSet<>(Arrays.asList("foreignkeycolumn")), new HashSet<>(referenceTableDataColumns));
    }

    @Test
    public void shouldNotStopTableCreationIfThereIsErrorOccuresForOthersTable() {
        TableData tableData = new TableData("tablename");
        TableData referenceTableData = new TableData("foreignkeytable");

        referenceTableData.addColumn(new TableColumn("foreignkeycolumn", "Integer", false, null));
        tableData.addColumn(new TableColumn(null, "Integer", false, null));

        tableGeneratorStep.createTables(Arrays.asList(tableData, referenceTableData));

        postgresJdbcTemplate.queryForList("SELECT * FROM \"foreignkeytable\"");

        expectedException.expect(BadSqlGrammarException.class);
        expectedException.expectMessage("user lacks privilege or object not found: tablename");
        postgresJdbcTemplate.queryForList("SELECT * FROM \"tablename\"");
    }
}