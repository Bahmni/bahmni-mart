SELECT
  patient_id,
  visit_id,
  bed_patient_info.bed_number,
  date_started,
  case when  (date_stopped = to_char(now(),'YYYY-MM-DD HH24:MI:SS')) then null
  else date_stopped end ,
  location,
  encounter_type_name,
  bed_tag_name,
  case when (tag_date_started is not null and tag_date_started < date_started) then date_started
  else tag_date_started END AS tag_start_date,
  CASE when (tag_date_stopped is not null and tag_date_stopped > date_stopped) then date_stopped
  when  (tag_date_stopped = to_char(now(),'YYYY-MM-DD HH24:MI:SS')) then null
  else tag_date_stopped end AS tag_end_date

FROM (
       select
         bpad.patient_id,
         bpad.visit_id,
         bpad.bed_id,
         bpad.bed_number,
         bpad.location,
         to_char(bpad.date_started, 'YYYY-MM-DD HH24:MI:SS') as date_started,
         to_char(case
                 when (bpad.date_stopped is null)
                   then now()
                 else bpad.date_stopped
                 end, 'YYYY-MM-DD HH24:MI:SS')               as date_stopped,
         pedd.encounter_type_name
       from bed_patient_assignment_default bpad
         inner join patient_encounter_details_default pedd on bpad.encounter_id = pedd.encounter_id

       UNION ALL

       SELECT
         bed_patient_assignment_info.patient_id,
         bed_patient_assignment_info.visit_id,
         bed_patient_assignment_info.bed_id,
         bed_patient_assignment_info.bed_number,
         bed_patient_assignment_info.location,
         to_char(bed_patient_assignment_info.date_started, 'YYYY-MM-DD HH24:MI:SS') as date_started,
         to_char(bed_patient_assignment_info.date_stopped, 'YYYY-MM-DD HH24:MI:SS') as date_stopped,
         ped.encounter_type_name
       FROM (SELECT
               patient_id,
               visit_id,
               date_started,
               date_stopped,
               location,
               bed_number,
               bed_id
             FROM bed_patient_assignment_default
            ) bed_patient_assignment_info
         INNER JOIN patient_encounter_details_default ped
           ON ped.patient_id = bed_patient_assignment_info.patient_id AND
              encounter_type_name = 'DISCHARGE' AND
              date(ped.encounter_datetime) = date(bed_patient_assignment_info.date_stopped)
     ) bed_patient_info
  left join (
              select
                bed_id,
                bed_number,
                bed_tag_name,
                to_char(date_created, 'YYYY-MM-DD HH24:MI:SS') as tag_date_started,
                to_char(case
                        when (date_stopped is null) then now()
                        else date_stopped
                        end, 'YYYY-MM-DD HH24:MI:SS') as tag_date_stopped
              from bed_tags_default
            ) taginfo on bed_patient_info.bed_id = taginfo.bed_id AND
                         (not
                         (taginfo.tag_date_started > bed_patient_info.date_stopped OR
                          taginfo.tag_date_stopped < bed_patient_info.date_started
                         ))
order by patient_id
