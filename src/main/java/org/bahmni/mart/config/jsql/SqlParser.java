package org.bahmni.mart.config.jsql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperation;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SqlParser {

    private static final Logger logger = LoggerFactory.getLogger(SqlParser.class);

    public static String getUpdatedReaderSql(List<String> columnsToIgnore, String readerSQL) {

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try {
            Select select = (Select) parserManager.parse(new StringReader(readerSQL));
            SelectBody selectBody = select.getSelectBody();

            return selectBody instanceof PlainSelect ?
                    getUpdatedReaderSqlFromPlainSelect(columnsToIgnore, (PlainSelect) selectBody) :
                    getUpdatedSql(columnsToIgnore, (SetOperationList) selectBody);

        } catch (JSQLParserException e) {
            logger.error("Unable to parse the reader sql: " + e.getMessage(), e);
        }

        return readerSQL;
    }

    private static String getUpdatedSql(List<String> columnsToIgnore, SetOperationList setOperationList)
            throws JSQLParserException {

        List<String> updatedSelectStatements = setOperationList.getSelects().stream().map(selectBody ->
                getUpdatedReaderSqlFromPlainSelect(columnsToIgnore, (PlainSelect) selectBody))
                .collect(Collectors.toList());

        return getReaderSql(setOperationList, updatedSelectStatements);
    }

    private static String getReaderSql(SetOperationList setOperationList, List<String> updatedSelectStatements)
            throws JSQLParserException {
        List<SetOperation> setOperations = setOperationList.getOperations();
        Iterator<String> iterator = updatedSelectStatements.iterator();
        StringBuilder finalReaderSqlBuilder = new StringBuilder();

        for (SetOperation setOperation : setOperations) {
            assertSelectStatementExist(iterator);
            finalReaderSqlBuilder.append(iterator.next());
            finalReaderSqlBuilder.append(" " + setOperation.toString() + " ");
        }
        assertSelectStatementExist(iterator);
        finalReaderSqlBuilder.append(iterator.next());

        return finalReaderSqlBuilder.toString();
    }

    private static void assertSelectStatementExist(Iterator<String> iterator) throws JSQLParserException {
        if (!iterator.hasNext()) {
            throw new JSQLParserException();
        }
    }

    private static String getUpdatedReaderSqlFromPlainSelect(List<String> columnsToIgnore, PlainSelect selectBody) {
        List<String> updatedColumns = new ArrayList<>();
        List<String> actualColumns = new ArrayList<>();
        List<SelectItem> selectItems = selectBody.getSelectItems();

        for (SelectItem selectItem : selectItems) {
            SelectItemVisitorImpl selectItemVisitor = new SelectItemVisitorImpl();
            selectItem.accept(selectItemVisitor);

            if (!columnsToIgnore.contains(getColumnName(selectItemVisitor))) {
                updatedColumns.add(selectItem.toString());
            }

            actualColumns.add(selectItem.toString());
        }

        return getUpdatedSQL(updatedColumns, getSqlAfterFromClause(selectBody, actualColumns), selectBody);
    }

    private static String getSqlAfterFromClause(PlainSelect selectBody, List<String> actualColumns) {
        String selectString = getSelectString(selectBody) + StringUtils.join(actualColumns, ", ");
        String selectStatement = selectBody.toString();

        return selectStatement.substring(selectString.length() + 1, selectStatement.length());
    }

    private static String getSelectString(PlainSelect selectBody) {
        return selectBody.getDistinct() == null ? "SELECT " : "SELECT DISTINCT ";
    }

    private static String getColumnName(SelectItemVisitorImpl selectItemVisitor) {
        String columnName = selectItemVisitor.getAlias() != null ?
                selectItemVisitor.getAlias() :
                selectItemVisitor.getExpressionVisitor().getColumnName();

        return columnName.charAt(0) == '`' ?
                columnName.substring(1, columnName.length() - 1) : columnName;
    }

    private static String getUpdatedSQL(List<String> updatedColumns, String query, PlainSelect selectBody) {
        return updatedColumns.isEmpty() ? "" : String.format("%s%s %s", getSelectString(selectBody),
                StringUtils.join(updatedColumns, ", "), query);
    }
}
