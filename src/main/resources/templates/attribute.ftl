<@compress single_line=true>
SELECT
    <#list input.tableData.columns as column>
        <#if column.name?contains('_id')>
            ${column.name}
            <#assign primary_key = column.name>
        <#else >
        MAX(if( name =  '${getProcessedName(column.name)}' AND value_table.voided = 0,
                    if  (value_table.${getProcessedName(input.eavAttributes.valueColumnName)} REGEXP '^[[:digit:]]*$' AND
            ${getType('${getProcessedName(column.name)}', '${getProcessedName(input.eavAttributes.attributeTypeTableName)}',
            '${getProcessedName(input.typeColumnName)}')} REGEXP '${getProcessedName(input.dataTypeValue)}' AND
            ${getConceptName(input.eavAttributes.valueColumnName)} IS NOT NULL,
            ${getConceptName(input.eavAttributes.valueColumnName)},
                        value_table.${input.eavAttributes.valueColumnName})
        , NULL)) AS '${getProcessedName(column.name)}'
        </#if>
        <#if input.tableData.columns?seq_index_of(column) <=  input.tableData.columns?size - 2 >,</#if>
    </#list>
FROM ${input.eavAttributes.attributeTableName} as value_table INNER JOIN  ${input.eavAttributes.attributeTypeTableName} as type_table
WHERE value_table.${input.eavAttributes.valueTableJoiningId} = type_table.${input.eavAttributes.typeTableJoiningId}
GROUP BY ${primary_key}
</@compress>

<#function getConceptName conceptId>
    <#assign conceptName = "(select COALESCE(c2.name, c1.name) as 'name' from concept_name c1 left join concept_name c2 on c1.concept_id = c2.concept_id  AND c2.concept_name_type = 'SHORT' AND c2.voided != 1 where c1.concept_name_type = 'FULLY_SPECIFIED' and c1.voided = 0 AND c1.concept_id = value_table.${conceptId} and c2.locale = 'en' and c1.locale = 'en')">
    <#return conceptName>
</#function>

<#function getType attributeName typeTableName typeColumnName>
    <#assign type="(select ${typeColumnName} from ${typeTableName} where name = '${attributeName}')">
    <#return type>
</#function>

<#function getProcessedName name>
    <#return name?replace("'", "''")>
</#function>

