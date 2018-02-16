SELECT obs0.obs_id,obs${input.depthToParent}.obs_id as parent_obs_id
FROM obs obs0
<#if input.depthToParent &gt; 0>
    <#list 1..input.depthToParent as x>
        INNER JOIN obs obs${x} on ( obs${x}.obs_id=obs${x-1}.obs_group_id and obs${x}.voided=0 )
    </#list>
</#if>
WHERE obs0.concept_id=${input.formName.id?c}
AND obs0.voided = 0
<#if input.parent?has_content>
    AND obs${input.depthToParent}.concept_id=${input.parent.formName.id?c}
</#if>
