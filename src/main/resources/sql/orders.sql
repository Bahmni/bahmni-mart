SELECT DISTINCT
  o.patient_id,
  o.date_created,
  o.encounter_id,
  v.visit_id,
  vt.name               AS visit_name,
  cv2.concept_full_name AS type_of_test,
  cv1.concept_full_name AS panel_name,
  cv.concept_full_name  AS test_name
FROM (SELECT
        patient_id,
        date_created,
        encounter_id,
        concept_id
      FROM orders where order_type_id=:orderTypeId) AS o
  JOIN visit v ON (v.patient_id = o.patient_id)
  JOIN visit_type vt ON vt.visit_type_id = v.visit_type_id
  JOIN concept c ON c.concept_id = o.concept_id
  JOIN concept_set cs ON cs.concept_set = o.concept_id
  JOIN concept_view cv ON cv.concept_id = cs.concept_id
  JOIN concept_view cv1 ON cv1.concept_id = o.concept_id
  JOIN concept_view cv2 ON cv2.concept_id = (SELECT concept_set
                                             FROM concept_set
                                             WHERE concept_id = o.concept_id
                                             LIMIT 1)
UNION

SELECT DISTINCT
  o.patient_id,
  o.date_created,
  o.encounter_id,
  v.visit_id,
  vt.name               AS visit_name,
  cv1.concept_full_name AS type_of_test,
  NULL                  AS panel_name,
  cv.concept_full_name  AS test_name
FROM (SELECT
        patient_id,
        date_created,
        encounter_id,
        concept_id
      FROM orders where order_type_id=:orderTypeId) AS o
  JOIN visit v ON (v.patient_id = o.patient_id)
  LEFT JOIN visit_type vt ON vt.visit_type_id = v.visit_type_id
  JOIN concept_view cv ON cv.concept_id = o.concept_id
  INNER JOIN concept c ON c.concept_id = o.concept_id AND c.is_set = 0
  INNER JOIN concept_view cv1 ON cv1.concept_id = c.concept_id