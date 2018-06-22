SELECT
  sa.patient_id,
  sb.surgical_block_id,
  primary_provider_name,
  sb.creator_name          AS block_creator_name,
  location_name,
  block_starttime          AS block_start_time,
  block_endtime            AS block_end_time,
  sb.date_created          AS block_date_created,
  sb.date_changed          AS block_date_changed,
  sb.changed_by            AS block_changed_by,
  saa.*,
  sa.sort_weight           AS surgery_sort_weight,
  sa.status                AS surgery_status,
  sa.actual_start_datetime AS surgery_actual_startime,
  sa.actual_end_datetime   AS surgery_actual_endtime,
  sa.notes                 AS surgery_notes,
  sa.date_created          AS surgery_datecreated,
  sa.date_changed          AS surgery_date_changed,
  sa.creator_name          AS sugery_creator_name,
  sa.changed_by            AS surgery_changed_by
FROM surgical_block_default sb LEFT JOIN surgical_appointment_default sa ON sa.surgical_block_id = sb.surgical_block_id
  LEFT JOIN surgical_appointment_attributes saa ON saa.surgical_appointment_id = sa.surgical_appointment_id