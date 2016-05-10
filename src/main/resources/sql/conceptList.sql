select childConcept.concept_id as id,childConcept.concept_full_name as name, childConceptRaw.is_set as isset
from concept_view parentConcept
  inner join concept_set cs on parentConcept.concept_id = cs.concept_set
  inner join concept_view childConcept on cs.concept_id=childConcept.concept_id
  inner join concept childConceptRaw on childConcept.concept_id = childConceptRaw.concept_id
where parentConcept.concept_full_name = '?' and childConcept.retired=0;
