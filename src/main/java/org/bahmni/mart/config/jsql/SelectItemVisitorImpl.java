package org.bahmni.mart.config.jsql;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

public class SelectItemVisitorImpl implements SelectItemVisitor {


    private String alias;

    private ExpressionVisitorImpl expressionVisitor;

    public String getAlias() {
        return alias;
    }

    public ExpressionVisitorImpl getExpressionVisitor() {
        return expressionVisitor;
    }

    @Override
    public void visit(AllColumns allColumns) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        Alias alias = selectExpressionItem.getAlias();
        if (alias != null)
            this.alias = alias.getName();
        expressionVisitor = new ExpressionVisitorImpl();
        selectExpressionItem.getExpression().accept(expressionVisitor);
    }
}
