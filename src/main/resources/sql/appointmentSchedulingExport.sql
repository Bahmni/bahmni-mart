SELECT
  pa.patient_id                                   AS `patient_id`,
  DATE_FORMAT(pa.start_date_time, '%d %b %Y')     AS `appointment_date`,
  DATE_FORMAT(pa.start_date_time, '%h:%i %p')     AS `appt_start_time`,
  DATE_FORMAT(pa.end_date_time, '%h:%i %p')       AS `appt_end_time`,
  DATE_FORMAT(pa.date_changed, '%d %b %Y')        AS `last_date_modified`,
  providerNames.name                              AS `provider`,
  CONCAT('"', app_ser.name, '"')                  AS `service`,
  CONCAT('"', ast.name, '"')                      AS `service_sub_type`,
  pa.status                                       AS `appt_status`,
  IF(pa.appointment_kind = 'WalkIn', 'Yes', 'No') AS `walk_in`,
  CONCAT('"', l.name, '"')                        AS `appt_location`,
  CONCAT('"', pa.comments, '"')                   AS `appt_notes`
FROM patient_appointment pa
  LEFT JOIN (
              SELECT
                p.provider_id,
                CONCAT('\"', CONCAT(pn.given_name, ' ', pn.family_name), '\"') AS `name`
              FROM provider p
                INNER JOIN person_name pn ON p.person_id = pn.person_id
            ) providerNames ON pa.provider_id = providerNames.provider_id
  INNER JOIN appointment_service app_ser ON pa.appointment_service_id = app_ser.appointment_service_id
  LEFT JOIN appointment_service_type ast ON pa.appointment_service_type_id = ast.appointment_service_type_id
  LEFT JOIN location l ON pa.location_id = l.location_id;