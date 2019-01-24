<@compress single_line=true>
    <#function filterColumnsWithPrimaryKey columns>
        <#local result = []>
        <#list columns as column>
            <#if column.isPrimaryKey()>
                <#local result = result + [column]>
            </#if>
        </#list>
        <#return result>
    </#function>
    <#function filterForeignKeyColumns columns>
        <#local result = []>
        <#list columns as column>
            <#if column.reference?has_content>
                <#local result = result + [column]>
            </#if>
        </#list>
        <#return result>
    </#function>
DROP TABLE IF EXISTS "${input.name}" CASCADE;
<#if input.getColumns()?size &gt; 0>
CREATE TABLE "${input.name}"(
    <#list input.columns as column>
       "${column.name}" ${(column.type)!}
        <#--<#if column.reference?has_content>REFERENCES "${column.reference.referenceTable}" ("${column.reference-->
        <#--.referenceColumn}") ON DELETE CASCADE</#if>-->
        <#if input.columns?seq_index_of(column) <= input.columns?size - 2 >,</#if>
    </#list>

    <#assign foreignKeyColumns = filterForeignKeyColumns(input.getColumns()) >
    <#if foreignKeyColumns?size &gt; 0>
        ,FOREIGN KEY (
            <#list foreignKeyColumns as foreignKeyColumn>
                ${foreignKeyColumn.name}
                <#if foreignKeyColumns?seq_index_of(foreignKeyColumn) <= foreignKeyColumns?size - 2 >,</#if>
            </#list>
        )
        <#assign firstForeginKeyColumn = foreignKeyColumns[0]>
        REFERENCES "${firstForeginKeyColumn.reference.referenceTable}" (
            <#list foreignKeyColumns as foreignKeyColumn>
                ${foreignKeyColumn.reference.referenceColumn}
                <#if foreignKeyColumns?seq_index_of(foreignKeyColumn) <= foreignKeyColumns?size - 2 >,</#if>
            </#list>
        )  ON DELETE CASCADE
    </#if>

    <#assign primaryKeyColumns = filterColumnsWithPrimaryKey(input.getColumns()) >
    <#if primaryKeyColumns?size &gt; 0>
        ,PRIMARY KEY(
        <#list primaryKeyColumns as primaryKeyCoumn>
            ${primaryKeyCoumn.name}
            <#if primaryKeyColumns?seq_index_of(primaryKeyCoumn) <= primaryKeyColumns?size - 2 >,</#if>
        </#list>
    )
    </#if>

);
</#if>
</@compress>
