package org.bahmni.mart.config.jsql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperation;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SqlParser.class, StringUtils.class})
public class SqlParserTest {

    @Mock
    private CCJSqlParserManager ccjSqlParserManager;

    @Mock
    private StringReader stringReader;

    @Mock
    private Select select;

    @Mock
    private PlainSelect plainSelect;

    @Mock
    private PlainSelect plainSelect2;

    @Mock
    private SelectItem selectItem1;

    @Mock
    private SelectItem selectItem2;

    @Mock
    private SelectItemVisitorImpl selectItemVisitor;

    @Mock
    private ExpressionVisitorImpl expressionVisitor;

    @Mock
    private SetOperationList setOperationList;

    @Mock
    private SetOperation setOperation;

    @Test
    public void shouldReturnUpdatedSql() throws Exception {
        List<String> columnsToIgnore = Collections.singletonList("patient_id");
        String readerSql = "SELECT patient_id, patient_name AS name FROM patient";
        String expectedSql = "SELECT patient_name AS name FROM patient";

        mockStatic(StringUtils.class);

        whenNew(CCJSqlParserManager.class).withNoArguments().thenReturn(ccjSqlParserManager);
        whenNew(StringReader.class).withArguments(readerSql).thenReturn(stringReader);
        when(ccjSqlParserManager.parse(stringReader)).thenReturn(select);
        when(select.getSelectBody()).thenReturn(plainSelect);
        when(plainSelect.getSelectItems()).thenReturn(Arrays.asList(selectItem1, selectItem2));
        whenNew(SelectItemVisitorImpl.class).withNoArguments().thenReturn(selectItemVisitor);
        when(selectItemVisitor.getAlias())
                .thenReturn(null)
                .thenReturn("name")
                .thenReturn("name");
        when(selectItem1.toString()).thenReturn("patient_id");
        when(selectItem2.toString()).thenReturn("patient_name AS name");
        when(selectItemVisitor.getExpressionVisitor()).thenReturn(expressionVisitor);
        when(expressionVisitor.getColumnName()).thenReturn("patient_id");
        when(StringUtils.join(Arrays.asList("patient_id", "patient_name AS name"), ", "))
                .thenReturn("patient_id, patient_name AS name");
        when(plainSelect.toString()).thenReturn(readerSql);
        when(StringUtils.join(Arrays.asList("patient_name AS name"), ", "))
                .thenReturn("patient_name AS name");

        assertEquals(expectedSql, SqlParser.getUpdatedReaderSql(columnsToIgnore, readerSql));

        verify(ccjSqlParserManager, times(1)).parse(stringReader);
        verify(select, times(1)).getSelectBody();
        verify(plainSelect, times(1)).getSelectItems();
        verify(selectItemVisitor, times(3)).getAlias();
        verify(selectItemVisitor, times(1)).getExpressionVisitor();
        verify(expressionVisitor, times(1)).getColumnName();
    }

    @Test
    public void shouldReturnEmptyStringWhenAllColumnsAreIgnored() throws Exception {
        List<String> columnsToIgnore = Arrays.asList("patient_id", "name");
        String readerSql = "SELECT patient_id, patient_name AS name FROM patient";

        mockStatic(StringUtils.class);

        whenNew(CCJSqlParserManager.class).withNoArguments().thenReturn(ccjSqlParserManager);
        whenNew(StringReader.class).withArguments(readerSql).thenReturn(stringReader);
        when(ccjSqlParserManager.parse(stringReader)).thenReturn(select);
        when(select.getSelectBody()).thenReturn(plainSelect);
        when(plainSelect.getSelectItems()).thenReturn(Arrays.asList(selectItem1, selectItem2));
        whenNew(SelectItemVisitorImpl.class).withNoArguments().thenReturn(selectItemVisitor);
        when(selectItemVisitor.getAlias())
                .thenReturn(null)
                .thenReturn("name")
                .thenReturn("name");
        when(selectItem1.toString()).thenReturn("patient_id");
        when(selectItem2.toString()).thenReturn("patient_name AS name");
        when(selectItemVisitor.getExpressionVisitor()).thenReturn(expressionVisitor);
        when(expressionVisitor.getColumnName()).thenReturn("patient_id");
        when(StringUtils.join(Arrays.asList("patient_id", "patient_name AS name"), ", "))
                .thenReturn("patient_id, patient_name AS name");
        when(plainSelect.toString()).thenReturn(readerSql);
        when(StringUtils.join(Arrays.asList("patient_name AS name"), ", "))
                .thenReturn("patient_name AS name");

        assertEquals("", SqlParser.getUpdatedReaderSql(columnsToIgnore, readerSql));

        verify(ccjSqlParserManager, times(1)).parse(stringReader);
        verify(select, times(1)).getSelectBody();
        verify(plainSelect, times(1)).getSelectItems();
        verify(selectItemVisitor, times(3)).getAlias();
        verify(selectItemVisitor, times(1)).getExpressionVisitor();
        verify(expressionVisitor, times(1)).getColumnName();
    }

    @Test
    public void shouldReturnUpdatedSqlWhenGivenSqlHasBackTicksOnColumnAlias() throws Exception {
        List<String> columnsToIgnore = Collections.singletonList("patient_id");
        String readerSql = "SELECT DISTINCT patient_id, patient_name AS `name` FROM patient";
        String expectedSql = "SELECT DISTINCT patient_name AS `name` FROM patient";

        mockStatic(StringUtils.class);

        whenNew(CCJSqlParserManager.class).withNoArguments().thenReturn(ccjSqlParserManager);
        whenNew(StringReader.class).withArguments(readerSql).thenReturn(stringReader);
        when(ccjSqlParserManager.parse(stringReader)).thenReturn(select);
        when(select.getSelectBody()).thenReturn(plainSelect);
        when(plainSelect.getSelectItems()).thenReturn(Arrays.asList(selectItem1, selectItem2));
        whenNew(SelectItemVisitorImpl.class).withNoArguments().thenReturn(selectItemVisitor);
        when(selectItemVisitor.getAlias())
                .thenReturn(null)
                .thenReturn("`name`")
                .thenReturn("`name`");
        when(selectItem1.toString()).thenReturn("patient_id");
        when(selectItem2.toString()).thenReturn("patient_name AS `name`");
        when(selectItemVisitor.getExpressionVisitor()).thenReturn(expressionVisitor);
        when(expressionVisitor.getColumnName()).thenReturn("patient_id");
        when(StringUtils.join(Arrays.asList("patient_id", "patient_name AS `name`"), ", "))
                .thenReturn("patient_id, patient_name AS `name`");
        Distinct distinct = mock(Distinct.class);
        when(plainSelect.getDistinct()).thenReturn(distinct).thenReturn(distinct);
        when(plainSelect.toString()).thenReturn(readerSql);
        when(StringUtils.join(Arrays.asList("patient_name AS `name`"), ", "))
                .thenReturn("patient_name AS `name`");

        String updatedReaderSql = SqlParser.getUpdatedReaderSql(columnsToIgnore, readerSql);
        assertEquals(expectedSql, updatedReaderSql);

        verify(ccjSqlParserManager, times(1)).parse(stringReader);
        verify(select, times(1)).getSelectBody();
        verify(plainSelect, times(1)).getSelectItems();
        verify(selectItemVisitor, times(3)).getAlias();
        verify(selectItemVisitor, times(1)).getExpressionVisitor();
        verify(expressionVisitor, times(1)).getColumnName();
    }

    @Test
    public void shouldReturnUpdatedSqlWhenGivenSqlHasSetOperations12() throws Exception {
        List<String> columnsToIgnore = Collections.singletonList("id");
        String readerSql = "SELECT id, name FROM patient UNION SELECT id, name FROM visitor";
        String expectedSql = "SELECT name FROM patient UNION SELECT name FROM visitor";

        mockStatic(StringUtils.class);

        whenNew(CCJSqlParserManager.class).withNoArguments().thenReturn(ccjSqlParserManager);
        whenNew(StringReader.class).withArguments(readerSql).thenReturn(stringReader);
        when(ccjSqlParserManager.parse(stringReader)).thenReturn(select);
        when(select.getSelectBody()).thenReturn(setOperationList);
        when(setOperationList.getSelects()).thenReturn(Arrays.asList(plainSelect, plainSelect2));
        when(plainSelect.getSelectItems()).thenReturn(Arrays.asList(selectItem1, selectItem2));
        when(plainSelect2.getSelectItems()).thenReturn(Arrays.asList(selectItem1, selectItem2));
        whenNew(SelectItemVisitorImpl.class).withNoArguments().thenReturn(selectItemVisitor);
        when(selectItemVisitor.getAlias()).thenReturn(null);
        when(selectItem1.toString()).thenReturn("id");
        when(selectItem2.toString()).thenReturn("name");
        when(selectItemVisitor.getExpressionVisitor()).thenReturn(expressionVisitor);
        when(expressionVisitor.getColumnName())
                .thenReturn("id").thenReturn("name")
                .thenReturn("id").thenReturn("name");
        when(StringUtils.join(Arrays.asList("id", "name"), ", ")).thenReturn("id, name");
        when(plainSelect.toString()).thenReturn("SELECT id, name FROM patient");
        when(plainSelect2.toString()).thenReturn("SELECT id, name FROM visitor");
        when(StringUtils.join(Arrays.asList("name"), ", ")).thenReturn("name");

        when(setOperationList.getOperations()).thenReturn(Collections.singletonList(setOperation));
        when(setOperation.toString()).thenReturn("UNION");

        assertEquals(expectedSql, SqlParser.getUpdatedReaderSql(columnsToIgnore, readerSql));

        verify(ccjSqlParserManager, times(1)).parse(stringReader);
        verify(select, times(1)).getSelectBody();
        verify(setOperationList, times(1)).getSelects();
        verify(plainSelect, times(1)).getSelectItems();
        verify(plainSelect2, times(1)).getSelectItems();
        verify(selectItemVisitor, times(4)).getAlias();
        verify(selectItemVisitor, times(4)).getExpressionVisitor();
        verify(expressionVisitor, times(4)).getColumnName();
        verify(setOperationList, times(1)).getOperations();
    }

    @Test
    public void shouldThrowExceptionForIncorrectReaderSQL() throws Exception {
        String readerSql = "SELECT id, name FROM";
        List<String> columnsToIgnore = Collections.singletonList("id");

        whenNew(CCJSqlParserManager.class).withNoArguments().thenReturn(ccjSqlParserManager);
        whenNew(StringReader.class).withArguments(readerSql).thenReturn(stringReader);
        Logger logger = mock(Logger.class);
        setValueForFinalStaticField(SqlParser.class, "logger", logger);
        when(ccjSqlParserManager.parse(stringReader)).thenThrow(JSQLParserException.class);

        assertEquals(readerSql, SqlParser.getUpdatedReaderSql(columnsToIgnore, readerSql));

        verify(logger, times(1)).error(eq("Unable to parse the reader sql: null"),
                any(JSQLParserException.class));
        verify(ccjSqlParserManager, times(1)).parse(stringReader);
    }
}