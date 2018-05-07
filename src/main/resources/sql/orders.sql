SELECT DISTINCT
  o.patient_id,
  o.date_created,
  o.encounter_id,
  o.encounter_type_id,
  o.encounter_type_name,
  v.visit_id,
  vt.name               AS visit_type,
  vt.visit_type_id,
  cv2.concept_full_name AS type_of_test,
  cv1.concept_full_name AS panel_name,
  cv.concept_full_name  AS test_name
FROM (SELECT
        orders.patient_id,
        orders.date_created,
        orders.encounter_id,
        concept_id,
        order_type_id,
        encounter_type AS encounter_type_id,
        et.name        AS encounter_type_name
      FROM orders
        INNER JOIN encounter e ON orders.encounter_id = e.encounter_id AND e.voided = FALSE
        INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id AND et.retired = FALSE) AS o
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
WHERE o.order_type_id = :orderTypeId
UNION

SELECT DISTINCT
  o1.patient_id,
  o1.date_created,
  o1.encounter_id,
  o1.encounter_type_id,
  o1.encounter_type_name,
  v.visit_id,
  vt.name               AS visit_type,
  vt.visit_type_id,
  cv1.concept_full_name AS type_of_test,
  NULL                  AS panel_name,
  cv.concept_full_name  AS test_name
FROM (SELECT
        orders.patient_id,
        orders.date_created,
        orders.encounter_id,
        concept_id,
        order_type_id,
        encounter_type AS encounter_type_id,
        et.name        AS encounter_type_name
      FROM orders
        INNER JOIN encounter e ON orders.encounter_id = e.encounter_id AND e.voided = FALSE
        INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id AND et.retired = FALSE) AS o1
  JOIN visit v ON (v.patient_id = o1.patient_id)
  LEFT JOIN visit_type vt ON vt.visit_type_id = v.visit_type_id
  JOIN concept_view cv ON cv.concept_id = o1.concept_id
  INNER JOIN concept c ON c.concept_id = o1.concept_id AND c.is_set = 0
  INNER JOIN concept_view cv1 ON cv1.concept_id = c.concept_id
WHERE o1.order_type_id = :orderTypeId