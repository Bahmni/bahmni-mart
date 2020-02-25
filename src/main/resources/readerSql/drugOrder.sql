SELECT
       o.patient_id              AS patient_id,
       o.programId               AS program_id,
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
       o.orderer_id              AS orderer_id,
       o.orderer_name            AS orderer_name,
       o.visit_id                AS visit_id,
       o.visit_type              AS visit_type,
       o.encounter_type_id,
       o.encounter_type_name
from
(SELECT
       pp.patient_program_id                                                                   AS  patientProgramId ,
       program.program_id                                                                      AS  programId,
       program.name                                                                            AS  programName ,
       orders.encounter_id                                                                     AS  encounterId ,
       drug.name                                                                               AS  codedDrugName ,
       drug_order.drug_non_coded                                                               AS  nonCodedDrugName ,
       drug_order.dose                                                                         AS  dose ,
       drug_order.quantity                                                                     AS  quantity ,
       quantityUnitcn.name                                                                     AS  quantityUnits ,
       drug_order.duration                                                                     AS  duration ,
       durationUnitscn.name                                                                    AS  durationUnits ,
       dosecn.name                                                                             AS  doseUnits ,
       routecn.name                                                                            AS  route ,
       freqcn.name                                                                             AS  frequency ,
       CASE WHEN Date(orders.scheduled_date) IS NULL
                 THEN orders.date_activated
            ELSE orders.scheduled_date END                                                          AS  startDate ,
       orders.auto_expire_date                                                                 AS  calculatedEndDate ,
       orders.date_stopped                                                                     AS  dateStopped ,
       p.patient_id,
       orders.order_id,
       CASE WHEN obs.value_coded
                 THEN 'Yes'
            ELSE 'No' END                                                                           AS  dispense ,
       orders.instructions                                                                     AS  instructions ,
       stopped_order_cn.name                                                                   AS  stopped_order_reason ,
       stopped_order.order_reason_non_coded                                                    AS  order_reason_non_coded ,
       CASE
         WHEN LOCATE("additionalInstructions", drug_order.dosing_instructions)
                 THEN LEFT(SUBSTRING_INDEX(drug_order.dosing_instructions, '"', -2),
                           LENGTH(SUBSTRING_INDEX(drug_order.dosing_instructions, '"', -2)) - 2)
         ELSE '' END
    AS additional_instructions,
       orders.orderer                                                                          AS orderer_id,
       concat_ws(' ', ifnull(person_name.given_name, ''), ifnull(person_name.family_name, '')) AS  orderer_name ,
       en.visit_id                                                                             AS  visit_id ,
       en.encounter_type                                                                       AS encounter_type_id,
       et.name                                                                                 AS encounter_type_name,
       vt.name                                                                                 AS visit_type

FROM patient p
       LEFT JOIN patient_program pp ON pp.patient_id = p.patient_id AND pp.voided = FALSE
       LEFT JOIN program ON program.program_id = pp.program_id AND program.retired = FALSE
       INNER JOIN episode_patient_program epp on pp.patient_program_id = epp.patient_program_id
       INNER JOIN episode_encounter ee on ee.episode_id = epp.episode_id
       INNER JOIN encounter en on ee.encounter_id = en.encounter_id and en.voided = FALSE
       INNER JOIN encounter_type et ON en.encounter_type = et.encounter_type_id AND et.retired = FALSE
       INNER JOIN visit v ON en.visit_id = v.visit_id AND v.voided = FALSE
       INNER JOIN visit_type vt ON v.visit_type_id = vt.visit_type_id AND vt.retired = FALSE

       INNER JOIN orders ON orders.patient_id = p.patient_id AND orders.encounter_id = en.encounter_id AND
                            orders.voided = FALSE AND orders.order_action != "DISCONTINUE"
       INNER JOIN provider ON provider.provider_id = orders.orderer AND provider.retired = FALSE
       LEFT JOIN person_name ON person_name.person_id = provider.person_id AND person_name.voided = FALSE
       LEFT JOIN obs ON obs.order_id = orders.order_id AND obs.voided = FALSE AND obs.concept_id = (SELECT concept_id
                                                                                                    FROM concept_name
                                                                                                    WHERE
                                                                                                        name = "Dispensed" AND
                                                                                                        concept_name_type = "FULLY_SPECIFIED" AND
                                                                                                        locale = 'en')
       LEFT JOIN orders stopped_order ON stopped_order.patient_id = p.patient_id AND stopped_order.voided = 0 AND
                                         stopped_order.order_action = "DISCONTINUE" AND
                                         stopped_order.previous_order_id = orders.order_id
       JOIN concept c ON c.concept_id = orders.concept_id AND c.retired = FALSE
       INNER JOIN drug_order drug_order ON drug_order.order_id = orders.order_id
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
       LEFT JOIN location ln ON ln.location_id = en.location_id
       ) o
GROUP BY patient_id, order_id