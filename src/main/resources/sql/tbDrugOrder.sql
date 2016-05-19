SELECT
  MAX(IF(o.program_attribute_name = 'Registration Number', o.program_attribute_value, NULL)) AS 'regnum',
  o.drug                                                                                     AS 'tbdrug',
  o.dose                                                                                     AS 'tbdose',
  o.units                                                                                    AS unit,
  o.route                                                                                    AS 'tbroute',
  o.frequency                                                                                AS 'tbdrug_freq',
  DATE_FORMAT(o.startDate, '%d/%b/%Y')                                                       AS 'd_tbdrugstart',
  o.additional_instructions                                                                  AS 'addlinstr',
  DATE_FORMAT(o.stopDate, '%d/%b/%Y')                                                        AS 'd_tbdrugend',
  o.stopped_order_reason                                                                     AS 'reas_tbd_stop',
  o.order_reason_non_coded                                                                   AS 'id_aenum_reas_d_stop_oth'
FROM
  (SELECT
     IF(drug.name IS NULL, drug_order.drug_non_coded, coalesce(drug_code.code, drug.name))                      AS drug,
     drug.name AS drugName,
     drug_order.dose                                                                 AS dose,
     dcn.code                                                                             AS units,
     rou.code                                                                              AS route,
     fre.code                                                                             AS frequency,
     IF(Date(orders.scheduled_date) IS NULL, orders.date_activated, orders.scheduled_date) AS startDate,
     IF(Date(orders.date_stopped) IS NULL, orders.auto_expire_date, orders.date_stopped)   AS stopDate,
     pg_attr_type.name                                                                     AS program_attribute_name,
     pg_attr.value_reference                                                               AS program_attribute_value,
     pp.patient_program_id,
     pg_attr.attribute_type_id,
     pp.patient_id,
     prog.program_id,
     orders.order_id,
     stopped_reason.code                                                                   AS 'stopped_order_reason',
     CONCAT('\"',stopped_order.order_reason_non_coded, '\"')                              AS  'order_reason_non_coded',
     IF(LOCATE("additionalInstructions", drug_order.dosing_instructions),
        CONCAT('\"',TRIM(TRAILING '"}' FROM SUBSTRING_INDEX(drug_order.dosing_instructions, '"', -2)), '\"'),
        '')                                                                                AS additional_instructions,
     pp.date_enrolled
   FROM patient_program pp
     JOIN program prog ON pp.program_id = prog.program_id AND
                          prog.name IN ('Second-line TB treatment register', 'Basic management unit TB register')
     LEFT JOIN patient_program_attribute pg_attr ON pp.patient_program_id = pg_attr.patient_program_id
     LEFT JOIN program_attribute_type pg_attr_type
       ON pg_attr.attribute_type_id = pg_attr_type.program_attribute_type_id AND
          pg_attr_type.name IN ('Registration Number')
     JOIN episode_patient_program epp ON pp.patient_program_id = epp.patient_program_id
     JOIN episode_encounter ee ON ee.episode_id = epp.episode_id
     JOIN orders orders ON orders.patient_id = pp.patient_id AND orders.encounter_id = ee.encounter_id AND
                           orders.voided = 0 AND (orders.order_action) != "DISCONTINUE" AND
                           orders.concept_id IN (SELECT cs.concept_id
                                                 FROM concept_set cs JOIN concept_name c
                                                     ON c.name = 'All TB Drugs' AND c.concept_id = cs.concept_set
                           )
     LEFT JOIN orders stopped_order ON stopped_order.patient_id = pp.patient_id AND stopped_order.voided = 0 AND
                                       (stopped_order.order_action) = "DISCONTINUE" AND
                                       stopped_order.previous_order_id = orders.order_id
     LEFT JOIN drug_order drug_order ON drug_order.order_id = orders.order_id
     LEFT JOIN drug ON drug.concept_id = orders.concept_id
     LEFT JOIN concept_reference_term_map_view drug_code ON drug_code.concept_id = drug.concept_id and drug_code.concept_reference_source_name='EndTB-Export' and drug_code.concept_map_type_name= 'SAME-AS'
     LEFT JOIN concept_reference_term_map_view dcn ON dcn.concept_id = drug_order.dose_units and dcn.concept_reference_source_name='EndTB-Export' and dcn.concept_map_type_name= 'SAME-AS'

     LEFT JOIN concept_reference_term_map_view rou ON rou.concept_id = drug_order.route and rou.concept_reference_source_name='EndTB-Export' and rou.concept_map_type_name= 'SAME-AS'
     LEFT JOIN order_frequency ON order_frequency.order_frequency_id = drug_order.frequency
     LEFT JOIN concept_reference_term_map_view fre ON order_frequency.concept_id = fre.concept_id and fre.concept_reference_source_name='EndTB-Export' and fre.concept_map_type_name= 'SAME-AS'
     LEFT JOIN concept_reference_term_map_view stopped_reason ON stopped_order.order_reason = stopped_reason.concept_id and stopped_reason.concept_reference_source_name='EndTB-Export' and stopped_reason.concept_map_type_name= 'SAME-AS'
) o
  LEFT OUTER JOIN program_attribute_type pat ON o.attribute_type_id = pat.program_attribute_type_id
GROUP BY patient_id, program_id, order_id
ORDER BY patient_id, date_enrolled;