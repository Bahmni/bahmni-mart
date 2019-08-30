SELECT *
FROM (
       SELECT
         bpa.patient_id,
         bpa.encounter_id,
         bpa.bed_number,
         bpa.location,
         EXTRACT(YEAR FROM (SELECT age( bpa.date_started, TO_TIMESTAMP(pd.birthyear, 'yyyy')))) AS age_at_bed_assignment,
         age_group(bpa.date_started, TO_TIMESTAMP(pd.birthyear, 'yyyy')) AS age_group_at_bed_assignment,
         CASE WHEN ped.encounter_type_name = 'TRANSFER'
           THEN 'MOVEMENT'
         ELSE ped.encounter_type_name END AS action,
         bpa.date_started                 AS assigned_on,
         bpa.date_stopped                 AS discharged_on,
         bt.bed_tag_name                  AS bed_tags,
         /*
         'bed_tag_created' should be between 'assigned_on' and 'discharged_on'.
         If bed is tagged before 'assigned_on' then 'bed_tag_created' will be 'assigned_on'
         */
         CASE
         WHEN (bt.date_created IS NULL)
           THEN NULL
         WHEN (bt.date_created <= bpa.date_started AND bt.date_stopped IS NOT NULL AND
               bt.date_stopped < bpa.date_started)
           THEN NULL
         WHEN (bt.date_created <= bpa.date_started)
           THEN bpa.date_started
         WHEN (bpa.date_stopped IS NULL)
           THEN bt.date_created
         WHEN (bt.date_created BETWEEN bpa.date_started AND bpa.date_stopped)
           THEN bt.date_created
         WHEN (bt.date_created > bpa.date_stopped)
           THEN NULL
         END                              AS bed_tag_created,
         /*
         'bed_tag_removed' should be between 'assigned_on' and 'discharged_on'.
         If bed tag is removed after 'discharged_on' then 'bed_tag_removed' will be 'discharged_on'
         */
         CASE
         WHEN (bt.date_stopped IS NULL AND bpa.date_stopped IS NULL)
           THEN NULL
         WHEN (bt.date_stopped ISNULL and bpa.date_stopped IS NOT NULL)
           THEN bpa.date_stopped
         WHEN (bpa.date_stopped IS NULL AND bt.date_stopped IS NOT NULL)
           THEN bt.date_stopped
         WHEN bt.date_stopped < bpa.date_started
           THEN NULL
         WHEN (bt.date_stopped BETWEEN bpa.date_started AND bpa.date_stopped)
           THEN bt.date_stopped
         WHEN (bt.date_stopped >= bpa.date_stopped)
           THEN bpa.date_stopped
         END                              AS bed_tag_removed

       FROM bed_patient_assignment_default bpa LEFT JOIN bed_tags_default bt ON bpa.bed_id = bt.bed_id
         LEFT JOIN patient_encounter_details_default ped ON ped.encounter_id = bpa.encounter_id
  LEFT JOIN person_details_default pd ON pd.person_id = bpa.patient_id
     ) AS result
WHERE bed_tag_created IS NOT NULL
