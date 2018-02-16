SELECT
  o.patient_id                                 AS 'patient_id',
  o.programId                                  AS 'patient_prg_id',
  CONCAT('\"', o.drugCode, '\"')               AS 'drug_code',
  CONCAT('\"', o.drugName, '\"')               AS 'drug_name',
  o.dose                                       AS 'dose',
  o.units                                      AS 'unit',
  o.route                                      AS 'route',
  o.frequency                                  AS 'frequency',
  DATE_FORMAT(o.startDate, '%d/%b/%Y')         AS 'drug_start',
  o.additional_instructions                    AS 'additional_instr',
  DATE_FORMAT(o.stopDate, '%d/%b/%Y')          AS 'drug_stop',
  o.location                                   AS 'location',
  o.duration                                   AS 'duration',
  o.durationUnits                              AS 'duration_units',
  IF(o.dot, 'Yes', 'No')                       AS 'dot',
  IF(o.dispense, 'Yes', 'No')                  AS 'dispense',
  o.stopped_order_reason                       AS 'stop_reason',
  CONCAT('\"', o.order_reason_non_coded, '\"') AS 'stop_notes'
FROM
  (SELECT
     pp.patient_program_id                                                                 AS 'programId',
     drug_code.code                                                                        AS 'drugCode',
     IF(drug_code.code IS NOT NULL, drug.name, drug_order.drug_non_coded)                  AS 'drugName',
     drug_order.dose                                                                       AS 'dose',
     drug_order.duration                                                                   AS 'duration',
     durationUnitscn.name                                                                  AS 'durationUnits',
     coalesce(dcn.code, dosecn.name)                                                       AS 'units',
     coalesce(rou.code, routecn.name)                                                      AS 'route',
     coalesce(fre.code, freqcn.name)                                                       AS 'frequency',
     IF(Date(orders.scheduled_date) IS NULL, orders.date_activated, orders.scheduled_date) AS 'startDate',
     IF(Date(orders
             .date_stopped) IS NULL, orders.auto_expire_date, orders.date_stopped)         AS 'stopDate',
     pp.patient_id,
     orders.order_id,
     ln.name                                                                               AS 'location',
     drug_order.as_needed                                                                  AS 'dot',
     obs.value_coded                                                                       AS 'dispense',
     coalesce(stopped_reason.code, stopped_order_cn.name)                                  AS 'stopped_order_reason',
     stopped_order.order_reason_non_coded                                                  AS 'order_reason_non_coded',
     IF(LOCATE("additionalInstructions", drug_order.dosing_instructions),
        CONCAT('\"', TRIM(TRAILING '"}' FROM SUBSTRING_INDEX(drug_order.dosing_instructions, '"', -2)), '\"'),
        '')                                                                                AS additional_instructions,
     pp.date_enrolled
   FROM patient p
     JOIN patient_program pp ON pp.patient_id = p.patient_id AND pp.voided IS FALSE
     JOIN encounter e ON e.patient_id = p.patient_id AND e.voided IS FALSE
     JOIN orders ON orders.patient_id = pp.patient_id AND orders.encounter_id = e.encounter_id AND
                    orders.voided IS FALSE AND orders.order_action != "DISCONTINUE"
     LEFT JOIN obs ON obs.order_id = orders.order_id AND obs.voided IS FALSE AND obs.concept_id = (SELECT concept_id
                                                                                                   FROM concept_name
                                                                                                   WHERE
                                                                                                     name = "Dispensed")
     LEFT JOIN orders stopped_order ON stopped_order.patient_id = pp.patient_id AND stopped_order.voided = 0 AND
                                       stopped_order.order_action = "DISCONTINUE" AND
                                       stopped_order.previous_order_id = orders.order_id
     JOIN concept c ON c.concept_id = orders.concept_id AND c.retired IS FALSE
     LEFT JOIN drug_order drug_order ON drug_order.order_id = orders.order_id
     LEFT JOIN concept_name durationUnitscn ON durationUnitscn.concept_id = drug_order.duration_units AND
                                               durationUnitscn.concept_name_type = "FULLY_SPECIFIED" AND
                                               durationUnitscn.voided = 0
     LEFT JOIN drug ON drug.concept_id = orders.concept_id
     LEFT JOIN concept_reference_term_map_view drug_code
       ON drug_code.concept_id = drug.concept_id AND drug_code.concept_reference_source_name = 'MSF-INTERNAL' AND
          drug_code.concept_map_type_name = 'SAME-AS'
     LEFT JOIN concept_reference_term_map_view dcn
       ON dcn.concept_id = drug_order.dose_units AND dcn.concept_reference_source_name = 'MSF-INTERNAL' AND
          dcn.concept_map_type_name = 'SAME-AS'
     LEFT JOIN concept_name dosecn
       ON dosecn.concept_id = drug_order.dose_units AND dosecn.concept_name_type = "FULLY_SPECIFIED" AND
          dosecn.voided = 0
     LEFT JOIN concept_reference_term_map_view rou
       ON rou.concept_id = drug_order.route AND rou.concept_reference_source_name = 'MSF-INTERNAL' AND
          rou.concept_map_type_name = 'SAME-AS'
     LEFT JOIN concept_name routecn
       ON routecn.concept_id = drug_order.route AND routecn.concept_name_type = "FULLY_SPECIFIED" AND routecn.voided = 0
     LEFT JOIN order_frequency ON order_frequency.order_frequency_id = drug_order.frequency
     LEFT JOIN concept_reference_term_map_view fre
       ON order_frequency.concept_id = fre.concept_id AND fre.concept_reference_source_name = 'MSF-INTERNAL' AND
          fre.concept_map_type_name = 'SAME-AS'
     LEFT JOIN concept_name freqcn
       ON freqcn.concept_id = order_frequency.concept_id AND freqcn.concept_name_type = "FULLY_SPECIFIED" AND
          freqcn.voided = 0
     LEFT JOIN concept_name stopped_order_cn ON stopped_order_cn.concept_id = stopped_order.order_reason AND
                                                stopped_order_cn.concept_name_type = "FULLY_SPECIFIED" AND
                                                stopped_order_cn.voided = 0
     LEFT JOIN concept_reference_term_map_view stopped_reason ON stopped_order.order_reason = stopped_reason.concept_id
                                                                 AND stopped_reason.concept_reference_source_name =
                                                                     'MSF-INTERNAL' AND
                                                                 stopped_reason.concept_map_type_name = 'SAME-AS'
     LEFT JOIN location ln ON ln.location_id = e.location_id
  ) o
GROUP BY patient_id, order_id
ORDER BY patient_id, date_enrolled;
