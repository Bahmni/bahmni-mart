<@compress single_line=true>
DROP TABLE IF EXISTS "${input.name}" CASCADE;
<#if input.getColumns()?size &gt; 0>
CREATE TABLE "${input.name}"(
    <#list input.columns as column>
       "${column.name}" ${(column.type)!}
        <#if column.isPrimaryKey()>PRIMARY KEY</#if>
        <#if column.reference?has_content>REFERENCES "${column.reference.referenceTable}" ("${column.reference.referenceColumn}")</#if>
        <#if input.columns?seq_index_of(column) <= input.columns?size - 2 >,</#if>
    </#list>
);
</#if>
</@compress>
