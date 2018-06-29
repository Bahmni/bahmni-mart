package org.bahmni.mart.helper;

import freemarker.template.Configuration;
import org.bahmni.mart.CommonTestHelper;
import org.bahmni.mart.exception.BatchResourceException;
import org.bahmni.mart.table.domain.ForeignKey;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@PrepareForTest(FreeMarkerEvaluator.class)
@RunWith(PowerMockRunner.class)
public class DDLForFormFreeMarkerEvaluatorTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private FreeMarkerEvaluator freeMarkerEvaluator;

    @Before
    public void setUp() throws Exception {
        freeMarkerEvaluator = new FreeMarkerEvaluator<TableData>();
        Configuration configuration = new Configuration();
        configuration.setDirectoryForTemplateLoading(
                new File(FreeMarkerEvaluator.class.getResource("/templates").getPath()));
        CommonTestHelper.setValuesForMemberFields(freeMarkerEvaluator, "configuration", configuration);
    }

    @Test
    public void shouldThrowBatchExceptionIfTheFtlTemplateIsNotPresent() throws Exception {
        String templateName = "nonExisted.ftl";
        expectedException.expect(BatchResourceException.class);
        expectedException.expectMessage(String
                .format("Unable to continue generating a the template with name [%s]", templateName));

        freeMarkerEvaluator.evaluate(templateName, new TableData());
    }

    @Test
    public void emptyTableDataObjectWithNoNameAndNoneOfTheColumns() throws Exception {
        String templateName = "ddlForForm.ftl";
        expectedException.expect(BatchResourceException.class);
        expectedException.expectMessage(String
                .format("Unable to continue generating a the template with name [%s]", templateName));

        freeMarkerEvaluator.evaluate(templateName, new TableData());
    }


    @Test
    public void shouldDropTheTableIfTheUpdatedFormDoesntHaveAnyColumns() throws Exception {
        TableData tableData = new TableData();
        tableData.setName("formWithNoChildren");
        String generatedSql = freeMarkerEvaluator.evaluate("ddlForForm.ftl", tableData);
        assertNotNull(generatedSql);
        assertEquals("DROP TABLE IF EXISTS \"formWithNoChildren\" CASCADE;", generatedSql);
    }

    @Test
    public void shouldParseTableDataWithColumnsWithNoPrimaryAndNoForeignKey() throws Exception {
        TableData tableData = new TableData();
        tableData.setName("formWithChildren");
        ArrayList<TableColumn> columns = new ArrayList<>();
        columns.add(new TableColumn("patient_id", "integer", false, null));
        columns.add(new TableColumn("encounter_id", "integer", false, null));
        tableData.setColumns(columns);
        String generatedSql = freeMarkerEvaluator.evaluate("ddlForForm.ftl", tableData);
        assertNotNull(generatedSql);
        assertEquals("DROP TABLE IF EXISTS \"formWithChildren\" CASCADE; CREATE TABLE " +
                "\"formWithChildren\"( \"patient_id\" integer , \"encounter_id\" integer );", generatedSql);
    }

    @Test
    public void shouldParseTableDataWithColumnsWithPrimaryAndNoForeignKey() throws Exception {
        TableData tableData = new TableData();
        tableData.setName("formWithChildren");
        ArrayList<TableColumn> columns = new ArrayList<>();
        columns.add(new TableColumn("patient_id", "integer", true, null));
        columns.add(new TableColumn("encounter_id", "integer", false, null));
        tableData.setColumns(columns);
        String generatedSql = freeMarkerEvaluator.evaluate("ddlForForm.ftl", tableData);
        assertNotNull(generatedSql);
        assertEquals("DROP TABLE IF EXISTS \"formWithChildren\" CASCADE; CREATE TABLE " +
                "\"formWithChildren\"( \"patient_id\" integer PRIMARY KEY , \"encounter_id\" integer );", generatedSql);
    }

    @Test
    public void shouldParseTableDataWithColumnsWithPrimaryAndForeignKey() throws Exception {
        TableData tableData = new TableData();
        tableData.setName("formWithChildren");
        ArrayList<TableColumn> columns = new ArrayList<>();
        columns.add(new TableColumn("patient_id", "integer", true, null));
        columns.add(new TableColumn("encounter_id", "integer", false, new ForeignKey("id", "encounter")));
        tableData.setColumns(columns);
        String generatedSql = freeMarkerEvaluator.evaluate("ddlForForm.ftl", tableData);
        assertNotNull(generatedSql);
        assertEquals("DROP TABLE IF EXISTS \"formWithChildren\" CASCADE; CREATE TABLE " +
                "\"formWithChildren\"( \"patient_id\" integer PRIMARY KEY , \"encounter_id\" " +
                "integer REFERENCES \"encounter\" (\"id\") ON DELETE CASCADE );", generatedSql);
    }

}
