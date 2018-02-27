<@compress single_line=true>
    <#if input.recordList?size &gt; 0>
        <#list input.recordList as record>
        INSERT INTO "${input.getTableName()}" (
            <#list record?keys as prop>
            "${prop}"
                <#if record?keys?seq_index_of(prop) <= record?keys?size - 2 >,</#if>
            </#list> )
        VALUES (
            <#list record?keys as prop>
                <#if record[prop]?has_content>
                ${record[prop]!}
                <#else>
                null
                </#if>
                <#if record?keys?seq_index_of(prop) <= record?keys?size - 2 >,</#if>
            </#list> );
        </#list>
    </#if>
</@compress>