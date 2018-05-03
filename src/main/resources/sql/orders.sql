SELECT DISTINCT
  o.patient_id,
  o.date_created,
  o.encounter_id,
  v.visit_id,
  vt.name               AS visit_type,
  cv2.concept_full_name AS type_of_test,
  cv1.concept_full_name AS panel_name,
  cv.concept_full_name  AS test_name
FROM (SELECT
        patient_id,
        date_created,
        encounter_id,
        concept_id,
        order_type_id
      FROM orders) as o
  JOIN visit v ON (v.patient_id = o.patient_id)
  JOIN visit_type vt ON vt.visit_type_id = v.visit_type_id
  JOIN concept c ON c.concept_id = o.concept_id
  JOIN concept_set cs ON cs.concept_set = o.concept_id
  JOIN concept_view cv ON cv.concept_id = cs.concept_id
  JOIN concept_view cv1 ON cv1.concept_id = o.concept_id
  JOIN concept_view cv2 ON cv2.concept_id = (SELECT concept_set
                                             FROM concept_set
                                             WHERE concept_id = o.concept_id
                                             LIMIT 1) where o.order_type_id=:orderTypeId
UNION

SELECT DISTINCT
  o1.patient_id,
  o1.date_created,
  o1.encounter_id,
  v.visit_id,
  vt.name               AS visit_type,
  cv1.concept_full_name AS type_of_test,
  NULL                  AS panel_name,
  cv.concept_full_name  AS test_name
FROM (SELECT
        patient_id,
        date_created,
        encounter_id,
        concept_id,
        order_type_id
      FROM orders) as o1
  JOIN visit v ON (v.patient_id = o1.patient_id)
  LEFT JOIN visit_type vt ON vt.visit_type_id = v.visit_type_id
  JOIN concept_view cv ON cv.concept_id = o1.concept_id
  INNER JOIN concept c ON c.concept_id = o1.concept_id AND c.is_set = 0
  INNER JOIN concept_view cv1 ON cv1.concept_id = c.concept_id where o1.order_type_id=:orderTypeId