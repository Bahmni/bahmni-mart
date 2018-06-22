SELECT
  bpa.patient_id,
  bpa.bed_number,
  bpa.date_started,
  bpa.date_stopped,
  bpa.location,
  ped.encounter_type_name,
  bt.bed_tag_name,
  bt.date_created AS tag_start_date,
  bt.date_stopped AS tag_end_date
FROM bed_patient_assignment_default bpa INNER JOIN patient_encounter_details_default ped ON ped.encounter_id = bpa.encounter_id
  LEFT JOIN bed_tags_default bt ON bt.bed_id = bpa.bed_id
UNION (SELECT
         dateStopped.patient_id,
         dateStopped.bed_number,
         NULL            AS date_started,
         dateStopped.date_stopped,
         dateStopped.location,
         ped.encounter_type_name,
         bt.bed_tag_name,
         bt.date_created AS tag_start_date,
         bt.date_stopped AS tag_end_date
       FROM (SELECT
               patient_id,
               date_stopped AS date_stopped,
               location,
               bed_number,
               bed_id
             FROM bed_patient_assignment_default) dateStopped INNER JOIN patient_encounter_details_default ped
           ON date(ped.encounter_datetime) = date(dateStopped.date_stopped) AND encounter_type_name = 'DISCHARGE' AND
              ped.patient_id = dateStopped.patient_id
         LEFT JOIN bed_tags_default bt ON bt.bed_id = dateStopped.bed_id)
ORDER BY patient_id