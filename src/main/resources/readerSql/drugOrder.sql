SELECT
  o.patient_id              AS patient_id,
  o.patientProgramId        AS patient_program_id,
  o.programName             AS patient_program_name,
  o.order_id                AS order_id,
  o.codedDrugName           AS coded_drug_name,
  o.nonCodedDrugName        AS non_coded_drug_name,
  o.dose                    AS dose,
  o.doseUnits               AS dose_units,
  o.frequency               AS frequency,
  o.route                   AS route,
  o.startDate               AS start_date,
  o.calculatedEndDate       AS calculated_end_date,
  o.dateStopped             AS date_stopped,
  o.stopped_order_reason    AS stop_reason,
  o.order_reason_non_coded  AS stop_notes,
  o.duration                AS duration,
  o.durationUnits           AS duration_units,
  o.quantity                AS quantity,
  o.quantityUnits           AS quantity_units,
  o.instructions            AS instructions,
  o.additional_instructions AS additional_instructions,
  o.dispense                AS dispense,
  o.encounterId             AS encounter_id,
  o.orderer                 AS orderer
FROM
  (SELECT
     pp.patient_program_id                                                                 AS 'patientProgramId',
     program.name                                                                          AS 'programName',
     orders.encounter_id                                                                   AS 'encounterId',
     drug.name                                                                             AS 'codedDrugName',
     drug_order.drug_non_coded                                                             AS 'nonCodedDrugName',
     drug_order.dose                                                                       AS 'dose',
     drug_order.quantity                                                                   AS 'quantity',
     quantityUnitcn.name                                                                   AS 'quantityUnits',
     drug_order.duration                                                                   AS 'duration',
     durationUnitscn.name                                                                  AS 'durationUnits',
     dosecn.name                                                                           AS 'doseUnits',
     routecn.name                                                                          AS 'route',
     freqcn.name                                                                           AS 'frequency',
     IF(Date(orders.scheduled_date) IS NULL, orders.date_activated, orders.scheduled_date) AS 'startDate',
     orders.auto_expire_date                                                               AS 'calculatedEndDate',
     orders.date_stopped                                                                   AS 'dateStopped',
     pp.patient_id,
     orders.order_id,
     IF(obs.value_coded, 'Yes', 'No')                                                      AS 'dispense',
     orders.instructions                                                                   AS 'instructions',
     stopped_order_cn.name                                                                 AS 'stopped_order_reason',
     stopped_order.order_reason_non_coded                                                  AS 'order_reason_non_coded',
     IF(LOCATE("additionalInstructions", drug_order.dosing_instructions),
        CONCAT('\"', TRIM(TRAILING '"}' FROM SUBSTRING_INDEX(drug_order.dosing_instructions, '"', -2)), '\"'),
        '')                                                                                AS additional_instructions,
     pp.date_enrolled,
     person_name.given_name                                                                AS 'orderer'
   FROM patient p
     INNER JOIN patient_program pp ON pp.patient_id = p.patient_id AND pp.voided IS FALSE
     INNER JOIN program ON program.program_id = pp.program_id AND program.retired IS FALSE
     INNER JOIN encounter e ON e.patient_id = p.patient_id AND e.voided IS FALSE
     INNER JOIN orders ON orders.patient_id = pp.patient_id AND orders.encounter_id = e.encounter_id AND
                    orders.voided IS FALSE AND orders.order_action != "DISCONTINUE"
     INNER JOIN provider ON provider.provider_id = orders.orderer AND provider.retired IS FALSE
     LEFT JOIN person_name ON person_name.person_id = provider.person_id AND person_name.voided IS FALSE
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
     LEFT JOIN concept_name quantityUnitcn ON quantityUnitcn.concept_id = drug_order.quantity_units AND
                                              quantityUnitcn.concept_name_type = "FULLY_SPECIFIED" AND
                                              quantityUnitcn.voided = 0
     LEFT JOIN drug ON drug.concept_id = orders.concept_id
     LEFT JOIN concept_name dosecn
       ON dosecn.concept_id = drug_order.dose_units AND dosecn.concept_name_type = "FULLY_SPECIFIED" AND
          dosecn.voided = 0
     LEFT JOIN concept_name routecn
       ON routecn.concept_id = drug_order.route AND routecn.concept_name_type = "FULLY_SPECIFIED" AND routecn.voided = 0
     LEFT JOIN order_frequency ON order_frequency.order_frequency_id = drug_order.frequency
     LEFT JOIN concept_name freqcn
       ON freqcn.concept_id = order_frequency.concept_id AND freqcn.concept_name_type = "FULLY_SPECIFIED" AND
          freqcn.voided = 0
     LEFT JOIN concept_name stopped_order_cn ON stopped_order_cn.concept_id = stopped_order.order_reason AND
                                                stopped_order_cn.concept_name_type = "FULLY_SPECIFIED" AND
                                                stopped_order_cn.voided = 0
     LEFT JOIN location ln ON ln.location_id = e.location_id

  ) o
GROUP BY patient_id, order_id

