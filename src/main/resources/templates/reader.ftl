<@compress single_line=true>
    <#assign toBeTransformedTo = "concept_name">
    <#assign toBeTransformedFrom = "concept_id">
    <#assign tableName = input.name>
    SELECT
        <#list input.columns as column>
            <#if column.name?contains(toBeTransformedTo)>
                <#assign columnName = column.name>
            ${getTransformQueryForConceptName(tableName,columnName,toBeTransformedFrom,toBeTransformedTo)} as ${column.name}
            <#else>
            ${tableName}.${column.name}
            </#if>
            <#if input.columns?seq_index_of(column) <= input.columns?size - 2 >,</#if>
        </#list>
FROM ${tableName};
</@compress>
<#function getTransformQueryForConceptName tableName columnName toBeTransformedFrom toBeTransformedTo>
    <#assign transformQuery = "(SELECT cn.name FROM concept_name cn WHERE cn.concept_id = ${tableName}.${columnName?replace(toBeTransformedTo,toBeTransformedFrom)} AND cn.concept_name_type = 'FULLY_SPECIFIED')">
    <#return transformQuery>
</#function>

