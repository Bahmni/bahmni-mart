<@compress single_line=true>
CREATE OR REPLACE FUNCTION age_group(process_start_date TIMESTAMP WITHOUT TIME ZONE,
                                     birthdate          TIMESTAMP WITHOUT TIME ZONE)
  RETURNS CHARACTER VARYING
LANGUAGE plpgsql
AS $$
BEGIN
    <#if input.ageGroups?has_content>
        <#assign ageGroups = input.ageGroups>
    <#else>
        <#assign ageGroups = ["0-4","5-14", "15-24", "25-34", "35-44", "45-54", "55-64"]>
    </#if>
    <#list ageGroups as ageGroup>
        <#if ageGroup?index == 0>
            IF
        <#else>
            ELSEIF
        </#if>
        <#assign ageLimits= "${ageGroup}"?split("-")>
        <#assign ageLimitOne= "${ageLimits[0]}">
        <#assign ageLimitTwo= "${ageLimits[1]}">
    EXTRACT(YEAR FROM (age(process_start_date, birthdate))) BETWEEN ${ageLimitOne} AND ${ageLimitTwo}
    THEN RETURN '${ageLimitOne}-${ageLimitTwo}';
    </#list>
    ELSEIF EXTRACT(YEAR FROM (age(process_start_date, birthdate))) > ${ageLimitTwo}
    THEN RETURN '${ageLimitTwo}+';
    ELSE RETURN NULL;
    END IF;
END;
$$;
</@compress>
