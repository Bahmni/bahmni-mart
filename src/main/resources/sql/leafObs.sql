select o.concept_id as conceptId,
  o.obs_id as id,
  coalesce(DATE_FORMAT(o.value_datetime, '%d/%b/%Y'),o.value_numeric,o.value_text,cv.code,cvn.concept_full_name,cvn.concept_short_name) as value,
  ppa.value_reference as treatmentNumber,
  obs_con.concept_full_name as conceptName
from patient_program pp
join program_attribute_type pg_at on (pg_at.name = 'Registration Number')
left join  patient_program_attribute ppa  on (pp.patient_program_id = ppa.patient_program_id and pg_at.program_attribute_type_id=ppa.attribute_type_id)
join  episode_patient_program epp on (pp.patient_program_id = epp.patient_program_id)
join episode_encounter ep_en on (epp.episode_id = ep_en.episode_id)
join   obs o on (ep_en.encounter_id = o.encounter_id)
join concept_view obs_con on(o.concept_id = obs_con.concept_id)
left outer join concept codedConcept on o.value_coded = codedConcept.concept_id
left outer join concept_reference_term_map_view cv on (cv.concept_id = codedConcept.concept_id and cv.concept_map_type_name = 'SAME-AS' and cv.concept_reference_source_name = 'EndTB-Export')
left outer join concept_view cvn on (codedConcept.concept_id = cvn.concept_id)
where
  o.obs_id in (:childObsIds)
  and obs_con.concept_id  in (:leafConceptIds)
  and o.voided=0
  AND pp.voided = 0;
