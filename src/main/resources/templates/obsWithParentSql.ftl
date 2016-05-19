SELECT obs0.obs_id
FROM obs obs0
<#if form.depthToParent &gt; 0>
<#list 1..form.depthToParent as x>
INNER JOIN obs obs${x} on obs${x-1}.obs_id=obs${x}.obs_group_id
</#list>
</#if>
WHERE obs0.concept_id=${form.formName.id?c}
<#if form.parent?has_content>
AND obs${form.depthToParent}.concept_id=${form.parent.formName.id?c}
</#if>
