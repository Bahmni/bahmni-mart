SELECT
  output.patient_id,
  output.location,
  output.bed_id,
  output.action,
  DATE_FORMAT(output.admission_date, '%d %b %Y %h:%i %p') AS `admission_date`,
  DATE_FORMAT(output.discharge_date, '%d %b %Y %h:%i %p') AS `discharge_date`,
  output.bed_tag,
  DATE_FORMAT(output.bed_tag_start, '%d %b %Y %h:%i %p') AS `bed_tag_start`,
  DATE_FORMAT(output.bed_tag_end, '%d %b %Y %h:%i %p') AS `bed_tag_end`
FROM (
       SELECT
         e.patient_id                                  AS `patient_id`,
         bedDetails.locationName                       AS `location`,
         bedDetails.bedNumber                          AS `bed_id`,
         IF(et.name = 'TRANSFER', 'MOVEMENT', et.name) AS `action`,
         bedDetails.admission_date                     AS `admission_date`,
         bedDetails.discharge_date                     AS `discharge_date`,
         bedDetails.bed_tag                            AS `bed_tag`,
         bedDetails.bed_tag_start                      AS `bed_tag_start`,
         bedDetails.bed_tag_end                        AS `bed_tag_end`
       FROM
         encounter e
         INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id AND et.retired IS FALSE AND
                                         (et.name = 'ADMISSION' OR et.name = 'TRANSFER') AND
                                         e.voided IS FALSE AND et.retired IS FALSE
         LEFT OUTER JOIN (
                           SELECT
                             bpam.patient_id,
                             bpam.encounter_id,
                           bpam.date_started                AS `admission_date`,
                           bpam.date_stopped                AS `discharge_date`,
                           bedTagsInfo.name                 AS `bed_tag`,
                           bedTagsInfo.`Date Started`       AS `bed_tag_start`,
                           bedTagsInfo.`Date Stopped`       AS `bed_tag_end`,
                           l.name                           AS `locationName`,
                           b.bed_number                     AS `bedNumber`
                           FROM
                             bed_patient_assignment_map AS bpam
                             INNER JOIN bed_location_map AS bl ON bpam.bed_id = bl.bed_id AND bpam.voided IS FALSE
                             INNER JOIN bed b ON bpam.bed_id = b.bed_id AND b.voided IS FALSE
                             INNER JOIN location AS l ON bl.location_id = l.location_id AND l.retired IS FALSE
                             LEFT OUTER JOIN (SELECT
                                                btm.bed_id,
                                                bpam.bed_patient_assignment_map_id,
                                                bt.name,
                                                GREATEST(btm.date_created, bpam.date_started) AS `Date Started`,
                                                IF(btm.date_voided IS NULL AND
                                                   bpam.date_stopped IS NULL,
                                                   NULL,
                                                   LEAST(IFNULL(btm.date_voided, now()),
                                                         IFNULL(bpam.date_stopped,
                                                                now())))                      AS `Date Stopped`
                                              FROM bed_tag_map btm
                                                INNER JOIN bed_tag bt
                                                  ON btm.bed_tag_id = bt.bed_tag_id AND bt.voided IS FALSE
                                                INNER JOIN bed_patient_assignment_map bpam
                                                  ON bpam.bed_id = btm.bed_id AND bpam.voided IS FALSE
                                                INNER JOIN person p
                                                  ON p.person_id = bpam.patient_id AND p.voided IS FALSE
                                                     AND NOT
                                                     (
                                                       btm.date_voided IS NOT NULL &&
                                                       btm.date_voided < bpam.date_started
                                                       OR
                                                       bpam.date_stopped IS NOT NULL &&
                                                       btm.date_created > bpam.date_stopped
                                                     )
                                             ) bedTagsInfo
                               ON bedTagsInfo.bed_patient_assignment_map_id = bpam.bed_patient_assignment_map_id

                         ) bedDetails
           ON bedDetails.patient_id = e.patient_id AND bedDetails.encounter_id = e.encounter_id
       UNION
        SELECT
          bpam.patient_id                                                     AS `patient_id`,
          l.name                                                              AS `location`,
          b.bed_number                                                        AS `bed_id`,
          'DISCHARGE'                                                         AS `action`,
          NULL                                                                AS `admission_date`,
          dischargeDetails.bed_discharge_date                                 AS `discharge_date`,
          bedTagsInfo.name                                                    AS `bed_tag`,
          bedTagsInfo.`Date Started`                                          AS `bed_tag_start`,
          bedTagsInfo.`Date Stopped`                                          AS `bed_tag_end`
          FROM
          bed_patient_assignment_map bpam
            INNER JOIN
          (
            SELECT
              bpam.patient_id,
              MAX(bpam.date_stopped) AS `bed_discharge_date`,
              dischargeTimes.date_created
            FROM bed_patient_assignment_map bpam
              INNER JOIN (
                           SELECT e.patient_id, e.date_created FROM encounter e
                             INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.name = 'DISCHARGE'
                         ) dischargeTimes ON bpam.patient_id = dischargeTimes.patient_id AND bpam.date_stopped <= dischargeTimes.date_created
            GROUP BY bpam.patient_id,dischargeTimes.date_created
          ) dischargeDetails ON bpam.patient_id = dischargeDetails.patient_id AND bpam.date_stopped = dischargeDetails.bed_discharge_date
            INNER JOIN bed_location_map blm ON blm.bed_id = bpam.bed_id
            INNER JOIN location l ON blm.location_id = l.location_id AND l.retired IS FALSE
            INNER JOIN bed b ON bpam.bed_id = b.bed_id AND b.voided IS FALSE
            LEFT OUTER JOIN (SELECT
                               btm.bed_id,
                               bpam.bed_patient_assignment_map_id,
                               bt.name,
                               GREATEST(btm.date_created, bpam.date_started) AS `Date Started`,
                               IF(btm.date_voided IS NULL AND
                                  bpam.date_stopped IS NULL,
                                  NULL,
                                  LEAST(IFNULL(btm.date_voided, now()),
                                        IFNULL(bpam.date_stopped,
                                               now())))                      AS `Date Stopped`
                             FROM bed_tag_map btm
                               INNER JOIN bed_tag bt
                                 ON btm.bed_tag_id = bt.bed_tag_id AND bt.voided IS FALSE
                               INNER JOIN bed_patient_assignment_map bpam
                                 ON bpam.bed_id = btm.bed_id AND bpam.voided IS FALSE
                               INNER JOIN person p
                                 ON p.person_id = bpam.patient_id AND p.voided IS FALSE
                                    AND NOT
                                    (
                                      btm.date_voided IS NOT NULL &&
                                      btm.date_voided < bpam.date_started
                                      OR
                                      bpam.date_stopped IS NOT NULL &&
                                      btm.date_created > bpam.date_stopped
                                    )
                            ) bedTagsInfo
              ON bedTagsInfo.bed_patient_assignment_map_id = bpam.bed_patient_assignment_map_id
       ORDER BY patient_id,
         CASE
         WHEN admission_date IS NOT NULL
           THEN admission_date
         ELSE discharge_date
         END,
         CASE
         WHEN discharge_date IS NOT NULL
           THEN discharge_date
         ELSE now()
         END
     ) output;