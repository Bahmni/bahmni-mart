select distinct childConcept.concept_id as id,childConcept.concept_full_name as name, childConceptRaw.is_set as isset, COALESCE (cv.code , cn.concept_full_name ,cn.concept_short_name)as title
from concept_view parentConcept
  inner join concept_set cs on parentConcept.concept_id = cs.concept_set
  inner join concept_view childConcept on cs.concept_id=childConcept.concept_id
  inner join concept childConceptRaw on childConcept.concept_id = childConceptRaw.concept_id
  left outer join concept_reference_term_map_view cv on (cv.concept_id = childConceptRaw.concept_id  and cv.concept_map_type_name = 'SAME-AS' AND cv.concept_reference_source_name = 'MSF-INTERNAL')
  left outer join concept_view  cn on (cn.concept_id = childConceptRaw.concept_id)
where parentConcept.concept_full_name = :parentConceptName and childConcept.retired=0
order by cs.sort_weight;