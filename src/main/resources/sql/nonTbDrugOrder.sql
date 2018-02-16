SELECT
  MAX(IF(o.program_attribute_name = 'Registration Number', CONCAT('\"', o.program_attribute_value, '\"'),
         NULL))                                                       AS 'regnum',
  CONCAT('\"', o.drugName, '\"')                                      AS 'othdrug',
  IF(o.as_needed = 1, CONCAT('\"', o.dose, ' ,(PRN),', '\"'), o.dose) AS 'othdrugdose',
  o.units                                                             AS 'othdrugform',
  o.route                                                             AS 'othdrugroute',
  o.frequency                                                         AS 'othdrugfreq',
  o.duration,
  o.quantity                                                          AS 'othtotdrugqty',
  o.quantity_units                                                    AS 'othtotdrugform',
  o.additional_instructions,
  reason_admin.code                                                   AS 'reas_othdrug',
  DATE_FORMAT(o.startDate, '%d/%b/%Y')                                AS 'd_othdrugstart',
  DATE_FORMAT(o.stopDate, '%d/%b/%Y')                                 AS 'd_othdrugend'

FROM
  (SELECT
     IF(drug.name IS NULL, drug_order.drug_non_coded, drug.name)                                                   AS drugName,
     drug_order.dose                                                                                               AS dose,
     drug_order.as_needed,
     dcn.code                                                                                                      AS units,
     rou.code                                                                                                      AS route,
     fre.code                                                                                                      AS frequency,
     concat(drug_order.duration, '' '',
            du.concept_full_name)                                                                                  AS duration,
     IF(orders.auto_expire_date IS NULL, '' '', concat(
         drug_order.quantity))                                                                                     AS quantity,
     qu.code                                                                                                       AS 'quantity_units',
     IF(Date(orders.scheduled_date) IS NULL, orders.date_activated,
        orders.scheduled_date)                                                                                     AS startDate,
     IF(Date(orders.date_stopped) IS NULL, orders.auto_expire_date,
        orders.date_stopped)                                                                                       AS stopDate,
     pg_attr_type.name                                                                                             AS program_attribute_name,
     pg_attr.value_reference                                                                                       AS program_attribute_value,
     pp.patient_program_id,
     pg_attr.attribute_type_id,
     pp.patient_id,
     prog.program_id,
     orders.order_id,
     stopped_order.order_reason_non_coded,
     IF(LOCATE("additionalInstructions", drug_order.dosing_instructions),
        CONCAT('\"', TRIM(TRAILING '"}' FROM SUBSTRING_INDEX(drug_order.dosing_instructions, '"', -2)), '\"'),
        '')                                                                                                        AS additional_instructions,
     IF(LOCATE("{\"instructions", dosing_instructions),
        TRIM(LEADING '{"instructions":"' FROM SUBSTRING_INDEX(dosing_instructions, '"', +4)),
        '')                                                                                                        AS reason_for_administration,
     drug_order.dosing_instructions,
     pp.date_enrolled
   FROM patient_program pp
     JOIN program prog ON pp.program_id = prog.program_id AND
                          prog.name IN ('Second-line TB treatment register', 'Basic management unit TB register') AND
                          pp.voided = 0
     LEFT JOIN patient_program_attribute pg_attr
       ON pp.patient_program_id = pg_attr.patient_program_id AND pg_attr.voided = 0
     LEFT JOIN program_attribute_type pg_attr_type
       ON pg_attr.attribute_type_id = pg_attr_type.program_attribute_type_id AND
          pg_attr_type.name IN ('Registration Number')
     JOIN episode_patient_program epp ON pp.patient_program_id = epp.patient_program_id
     JOIN episode_encounter ee ON ee.episode_id = epp.episode_id
     JOIN orders orders ON orders.patient_id = pp.patient_id AND orders.encounter_id = ee.encounter_id AND
                           orders.voided = 0 AND (orders.order_action) != "DISCONTINUE" AND
                           orders.concept_id IN (SELECT cs.concept_id
                                                 FROM concept_set cs
                                                   JOIN concept_name c
                                                     ON c.name = 'All Other Drugs' AND c.concept_id = cs.concept_set
                                                        AND c.voided = 0)
     LEFT JOIN orders stopped_order ON stopped_order.patient_id = pp.patient_id AND stopped_order.voided = 0 AND
                                       (stopped_order.order_action) = "DISCONTINUE" AND
                                       stopped_order.previous_order_id = orders.order_id
     LEFT JOIN drug_order drug_order ON drug_order.order_id = orders.order_id
     LEFT JOIN drug ON drug.concept_id = orders.concept_id
     LEFT JOIN concept_reference_term_map_view dcn
       ON dcn.concept_id = drug_order.dose_units AND dcn.concept_reference_source_name = 'EndTB-Export' AND
          dcn.concept_map_type_name = 'SAME-AS'
     LEFT JOIN concept_reference_term_map_view qu
       ON qu.concept_id = drug_order.quantity_units AND qu.concept_reference_source_name = 'EndTB-Export' AND
          qu.concept_map_type_name = 'SAME-AS'
     LEFT JOIN concept_reference_term_map_view rou
       ON rou.concept_id = drug_order.route AND rou.concept_reference_source_name = 'EndTB-Export' AND
          rou.concept_map_type_name = 'SAME-AS'
     LEFT JOIN concept_view du ON du.concept_id = drug_order.duration_units
     LEFT JOIN order_frequency ON order_frequency.order_frequency_id = drug_order.frequency
     LEFT JOIN concept_reference_term_map_view fre
       ON order_frequency.concept_id = fre.concept_id AND fre.concept_reference_source_name = 'EndTB-Export' AND
          fre.concept_map_type_name = 'SAME-AS'
  ) o
  LEFT OUTER JOIN program_attribute_type pat ON o.attribute_type_id = pat.program_attribute_type_id
  LEFT JOIN concept_name cn ON cn.name = o.reason_for_administration AND cn.voided = 0
  LEFT JOIN concept_reference_term_map_view reason_admin
    ON reason_admin.concept_id = cn.concept_id AND reason_admin.concept_reference_source_name = 'EndTB-Export' AND
       reason_admin.concept_map_type_name = 'SAME-AS'
GROUP BY patient_id, program_id, order_id
ORDER BY patient_id, date_enrolled;