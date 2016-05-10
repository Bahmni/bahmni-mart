SELECT
  MAX(IF(o.program_attribute_name = 'Registration Number', o.program_attribute_value, NULL)) AS 'regnum',
  o.drugName AS 'othdrug',
  IF(o.as_needed = 1, CONCAT('\"',o.dose,' ,(PRN),','\"'), o.dose) as 'othdrugdose',
  o.units as 'othdrugform',
  o.route as 'othdrugroute',
  o.frequency as 'othdrugfreq',
  o.duration,
  o.quantity as 'othtotdrugdose',
  o.quantity_units as 'othtotdrugform',
  o.additional_instructions,
  reason_admin.code as 'reas_othdrug',
  DATE_FORMAT(o.startDate, '%d/%b/%Y') as 'd_othdrugstart',
  DATE_FORMAT(o.stopDate, '%d/%b/%Y') as 'd_othdrugend'

FROM
  (SELECT
     IF(drug.name is NULL,drug_order.drug_non_coded, drug.name) as drugName,
     IF(drug_order.dose IS NULL , drug_order.dosing_instructions, drug_order.dose) AS dose,
     drug_order.as_needed,
     dcn.code as units,
     rou.code as route,
     fre.code as frequency,
     concat(drug_order.duration ,'' '', du.concept_full_name) as duration,
     IF(orders.auto_expire_date IS NULL,'' '' ,concat(drug_order.quantity)) as quantity,
      qu.code as 'quantity_units',
     IF(Date(orders.scheduled_date) IS NULL, orders.date_activated, orders.scheduled_date) as startDate,
     IF(Date(orders.date_stopped) is NULL, orders.auto_expire_date,orders.date_stopped) as stopDate,
     pg_attr_type.name  as program_attribute_name,
     coalesce(pg_attr_cn.concept_short_name, pg_attr_cn.concept_full_name, pg_attr.value_reference) as program_attribute_value,
     pp.patient_program_id,
     pg_attr.attribute_type_id,
     pp.patient_id,
     prog.program_id,
     orders.order_id,
     stopped_order.order_reason_non_coded,
     IF(LOCATE("additionalInstructions",drug_order.dosing_instructions) ,TRIM(TRAILING '"}' FROM SUBSTRING_INDEX(drug_order.dosing_instructions, '"',-2)),'') as additional_instructions,
     IF(LOCATE("{\"instructions",dosing_instructions) ,TRIM(LEADING '{"instructions":"' FROM SUBSTRING_INDEX(dosing_instructions, '"',+4)),'') as reason_for_administration,
     drug_order.dosing_instructions,
     pp.date_enrolled
   FROM  patient_program pp
     JOIN program prog ON pp.program_id = prog.program_id AND prog.name in ('Second-line TB treatment register','Basic management unit TB register')
     LEFT JOIN patient_program_attribute pg_attr ON pp.patient_program_id = pg_attr.patient_program_id
     LEFT JOIN program_attribute_type pg_attr_type ON pg_attr.attribute_type_id = pg_attr_type.program_attribute_type_id and pg_attr_type.name in ('Registration Number')
     LEFT JOIN concept_view pg_attr_cn ON pg_attr.value_reference = pg_attr_cn.concept_id AND pg_attr_type.datatype LIKE "%Concept%"
     JOIN  episode_patient_program epp on pp.patient_program_id = epp.patient_program_id
     JOIN episode_encounter ee on ee.episode_id = epp.episode_id
     JOIN orders orders ON orders.patient_id = pp.patient_id and orders.encounter_id = ee.encounter_id and orders.voided = 0 and (orders.order_action) != "DISCONTINUE" and orders.concept_id in (select cs.concept_id from concept_set cs join concept_name c on c.name='All Other Drugs' and c.concept_id=cs.concept_set
     )
     LEFT JOIN orders stopped_order ON stopped_order.patient_id = pp.patient_id  and stopped_order.voided = 0 and (stopped_order.order_action) = "DISCONTINUE" and stopped_order.previous_order_id = orders.order_id
     LEFT JOIN drug_order drug_order ON drug_order.order_id = orders.order_id
     LEFT JOIN drug ON drug.concept_id = orders.concept_id
     LEFT JOIN concept_reference_term_map_view dcn  ON dcn.concept_id = drug_order.dose_units AND dcn.concept_reference_source_name='EndTB-Export' and dcn.concept_map_type_name= 'SAME-AS'
     LEFT JOIN concept_reference_term_map_view qu ON qu.concept_id = drug_order.quantity_units AND qu.concept_reference_source_name='EndTB-Export' and qu.concept_map_type_name= 'SAME-AS'
     LEFT JOIN concept_reference_term_map_view rou  ON rou.concept_id = drug_order.route AND rou.concept_reference_source_name='EndTB-Export' and rou.concept_map_type_name= 'SAME-AS'
     LEFT JOIN concept_view du  ON du.concept_id = drug_order.duration_units
     LEFT JOIN order_frequency ON order_frequency.order_frequency_id = drug_order.frequency
     LEFT JOIN concept_reference_term_map_view fre  ON order_frequency.concept_id = fre.concept_id AND fre.concept_reference_source_name='EndTB-Export' and fre.concept_map_type_name= 'SAME-AS'
   ) o
  LEFT OUTER JOIN program_attribute_type pat ON o.attribute_type_id = pat.program_attribute_type_id
  LEFT JOIN concept_name cn on cn.name=o.reason_for_administration
  LEFT JOIN concept_reference_term_map_view reason_admin on reason_admin.concept_id=cn.concept_id AND reason_admin.concept_reference_source_name='EndTB-Export' and reason_admin.concept_map_type_name= 'SAME-AS'
GROUP BY patient_id, program_id, order_id
ORDER BY patient_id, date_enrolled;