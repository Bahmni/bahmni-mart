SELECT
  concept_full_name     AS name,
  concept.concept_id    AS id,
  concept_datatype_name AS dataType,
  concept.is_set        AS isSet
FROM concept_view
  INNER JOIN concept ON concept.concept_id = concept_view.concept_id
WHERE concept_datatype_name = 'Text';