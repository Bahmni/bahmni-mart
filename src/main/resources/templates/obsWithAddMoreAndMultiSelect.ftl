<@compress single_line=true>

    SELECT obs_id FROM obs
    INNER JOIN concept c on c.concept_id = obs.concept_id
    WHERE obs.concept_id = ${input.formName.id?c} AND obs.voided = 0 and c.is_set = 0

    <#list input.fields as concept>
    UNION
    SELECT obs0.obs_id FROM obs obs0
    INNER JOIN obs obs1 on (obs1.obs_id = obs0.obs_group_id and obs1.voided = 0)
    WHERE obs0.concept_id = ${concept.id?c} AND obs0.voided = 0 AND obs1.concept_id = ${concept.parent.id?c}
    </#list>

</@compress>