SELECT
  pd.person_id            AS patient_id,
  pi."Patient_Identifier" AS patient_identifier,
  pd.gender,
  pd.birthdate            AS birth_date,
  pd.dead,
  pa.*,
  md.patient_program_id,
  md.encounter_id,
  md.encounter_type_name,
  md.order_id,
  md.orderer_name,
  md.coded_drug_name,
  md.non_coded_drug_name,
  md.dose,
  md.dose_units,
  md.frequency,
  md.route,
  md.start_date           AS medication_start_date,
  md.calculated_end_date  AS medication_calculated_end_date,
  md.date_stopped         AS medication_stopped_date,
  md.stop_reason,
  md.duration,
  md.duration_units,
  md.quantity,
  md.quantity_units,
  md.additional_instructions,
  md.dispense             AS is_dispensed,
  md.visit_id,
  md.visit_type

FROM person_details pd
  LEFT JOIN person_attributes pa ON pa.person_id = pd.person_id
  LEFT JOIN patient_identifier pi ON pi.patient_id = pd.person_id
  LEFT JOIN medication_data md ON md.patient_id = pd.person_id