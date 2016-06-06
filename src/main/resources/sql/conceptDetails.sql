select cn.concept_id as id,cn.name,c.is_set as isSet
from concept_name cn
  inner join concept c on cn.concept_id = c.concept_id
where cn.name in (:conceptNames) and cn.concept_name_type='FULLY_SPECIFIED' and cn.voided=0;
