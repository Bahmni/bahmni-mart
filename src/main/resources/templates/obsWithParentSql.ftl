SELECT
    obs0.obs_id
<#if input.depthToParent &gt;1>
        ,obs1.obs_id as parent_obs_id
</#if>
FROM obs obs0
<#if input.depthToParent &gt; 1>
    <#list 1..input.depthToParent-1 as x>
INNER JOIN obs obs${x} on ( obs${x}.obs_id=obs${x-1}.obs_group_id and obs${x}.voided=0 )
    </#list>
</#if>
WHERE obs0.concept_id=${input.formName.id?c}
      AND obs0.voided = 0
<#if input.parent?has_content && input.depthToParent &gt;1>
AND obs1.concept_id=${input.parent.formName.id?c}
AND obs${input.depthToParent-1}.concept_id=${input.rootForm.formName.id?c}
</#if>