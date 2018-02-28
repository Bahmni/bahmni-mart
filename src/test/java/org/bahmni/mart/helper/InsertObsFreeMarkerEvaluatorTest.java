package org.bahmni.mart.helper;

import freemarker.template.Configuration;
import org.bahmni.mart.CommonTestHelper;
import org.bahmni.mart.exception.BatchResourceException;
import org.bahmni.mart.exports.ObsRecordExtractorForTable;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InsertObsFreeMarkerEvaluatorTest {
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

        freeMarkerEvaluator.evaluate(templateName, new ObsRecordExtractorForTable("tableName"));
    }

    @Test
    public void shouldReturnEmptyStringForExtractorWithTableNameAndNoRecordList() throws Exception {
        ObsRecordExtractorForTable extractor = new ObsRecordExtractorForTable("tableName");
        assertEquals("", freeMarkerEvaluator.evaluate("insertObs.ftl", extractor));
    }


    @Test
    public void shouldReturnOneInsertStatementForExtractorWithTableNameAndOneRecord() throws Exception {
        String templateName = "insertObs.ftl";
        ObsRecordExtractorForTable extractor = new ObsRecordExtractorForTable("tableName");
        Map<String, String> record = new HashMap<>();
        record.put("column_name", "column_value");
        extractor.setRecordList(Arrays.asList(record));
        String expectedSql = "INSERT INTO \"tableName\" ( \"column_name\" ) VALUES ( column_value );";

        String generatedSql = freeMarkerEvaluator.evaluate(templateName, extractor);

        assertNotNull(generatedSql);
        assertEquals(expectedSql, generatedSql);
    }

    @Test
    public void shouldReturnMultipleInsertStatementsForExtractorWithTableNameAndMultipleRecords() throws Exception {
        String templateName = "insertObs.ftl";
        ObsRecordExtractorForTable extractor = new ObsRecordExtractorForTable("tableName");
        Map<String, String> record1 = new HashMap<>();
        record1.put("r1_column_name1", "r1_column_value1");
        record1.put("r1_column_name2", "r1_column_value2");
        record1.put("r1_column_name3", "r1_column_value3");
        Map<String, String> record2 = new HashMap<>();
        record2.put("r2_column_name1", "r2_column_value1");
        record2.put("r2_column_name2", "r2_column_value2");
        record2.put("r2_column_name3", "r2_column_value3");
        extractor.setRecordList(Arrays.asList(record1, record2));
        String expectedSql = "INSERT INTO \"tableName\" ( \"r1_column_name1\" , \"r1_column_name3\" , " +
                "\"r1_column_name2\" ) VALUES ( r1_column_value1 , r1_column_value3 , r1_column_value2 ); " +
                "INSERT INTO " + "\"tableName\" ( \"r2_column_name3\" , \"r2_column_name1\" , \"r2_column_name2\" )" +
                " VALUES ( r2_column_value3 , r2_column_value1 , r2_column_value2 );";

        String generatedSql = freeMarkerEvaluator.evaluate(templateName, extractor);

        assertNotNull(generatedSql);
        assertEquals(expectedSql, generatedSql);
    }

    @Test
    public void shouldReturnInsertStatementsWithNullValuesForRecordsWithNullValues() throws Exception {
        String templateName = "insertObs.ftl";
        ObsRecordExtractorForTable extractor = new ObsRecordExtractorForTable("tableName");
        Map<String, String> record1 = new HashMap<>();
        record1.put("r1_column_name1", null);
        record1.put("r1_column_name2", "r1_column_value2");
        record1.put("r1_column_name3", null);
        Map<String, String> record2 = new HashMap<>();
        record2.put("r2_column_name1", null);
        record2.put("r2_column_name2", null);
        record2.put("r2_column_name3", "r2_column_value3");
        extractor.setRecordList(Arrays.asList(record1, record2));
        String expectedSql = "INSERT INTO \"tableName\" ( \"r1_column_name1\" , \"r1_column_name3\" , " +
                "\"r1_column_name2\" ) VALUES ( null , null , r1_column_value2 ); " +
                "INSERT INTO \"tableName\" ( \"r2_column_name3\" , \"r2_column_name1\" , \"r2_column_name2\" ) " +
                "VALUES ( r2_column_value3 , null , null );";

        String generatedSql = freeMarkerEvaluator.evaluate(templateName, extractor);

        assertNotNull(generatedSql);
        assertEquals(expectedSql, generatedSql);
    }

}
