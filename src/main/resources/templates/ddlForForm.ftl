<@compress single_line=true>

DROP TABLE IF EXISTS "${input.name}" CASCADE;
<#if input.getColumns()?size &gt; 0>
CREATE TABLE "${input.name}"(
    <#list input.columns as column>
       "${column.name}" ${(column.type)!}
        <#if column.reference?has_content>REFERENCES "${column.reference.referenceTable}" ("${column.reference
        .referenceColumn}") ON DELETE CASCADE</#if>
        <#if input.columns?seq_index_of(column) <= input.columns?size - 2 >,</#if>
    </#list>

    <#assign primaryKeyColumns = input.getPrimaryKeyColumns() >
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
