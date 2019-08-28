SELECT
  pd.person_id AS patient_id,
  pd.gender,
  pd.birthyear               AS birth_year,
   EXTRACT(YEAR FROM (SELECT age( sb.block_starttime, TO_DATE(CONCAT('01-01-', pd.birthyear), 'dd-MM-yyyy')))) AS age_at_surgery,
  age_group(sb.block_starttime, TO_DATE(CONCAT('01-01-', pd.birthyear), 'dd-MM-yyyy')) AS age_group_at_surgery,
  pd.dead,
  pa.*,
  sa.surgical_block_id,
  sb.primary_provider_name,
  sb.location_name,
  sb.creator_name            AS block_creator_name,
  sb.date_created            AS block_date_created,
  sb.date_changed            AS block_date_changed,
  sb.changed_by              AS block_changed_by,
  sb.block_starttime         AS block_start_time,
  sb.block_endtime           AS block_end_time,
  saa.*,
  sa.sort_weight             AS surgery_sort_weight,
  sa.status                  AS surgery_status,
  sa.actual_start_datetime   AS surgery_actual_start_time,
  sa.actual_end_datetime     AS surgery_actual_end_time,
  sa.notes                   AS surgery_notes,
  sa.date_created            AS surgery_date_created,
  sa.date_changed            AS surgery_date_changed,
  sa.creator_name            AS surgery_creator_name,
  sa.changed_by              AS surgery_date_changed_by

FROM person_details_default pd

  LEFT JOIN person_attributes pa on pa.person_id = pd.person_id
  LEFT JOIN surgical_appointment_default sa on sa.patient_id = pd.person_id
  LEFT JOIN surgical_block_default sb on sb.surgical_block_id = sa.surgical_block_id
  LEFT JOIN surgical_appointment_attributes saa on saa.surgical_appointment_id = sa.surgical_appointment_id
