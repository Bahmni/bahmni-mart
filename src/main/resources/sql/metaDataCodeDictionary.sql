SELECT  CONCAT('\"', cv.concept_full_name, '\"')  AS fully_specified_name,
        CONCAT('\"', cv.concept_short_name, '\"') AS question,
        CONCAT( '\"',
                REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(cv.description, '"', ' '), '\t', ' '), '\n', ' '), '\r', ' '), ',', ' '),
                '\"')        AS description,
        crtv.code                                 AS question_code,
        CONCAT('\"', cn.name, '\"')               AS answer,
        crtv_answer.code                          AS answer_code
FROM    concept_view cv
  INNER JOIN
  concept_reference_term_map_view crtv ON   cv.concept_id = crtv.concept_id AND
                                            crtv.concept_reference_source_name = 'EndTB-Export' AND
                                            crtv.concept_map_type_name = 'SAME-AS'
  JOIN
  concept_answer ca ON cv.concept_id = ca.concept_id
  LEFT OUTER JOIN
  concept_name cn ON  ca.answer_concept = cn.concept_id AND
                      cn.concept_name_type = 'FULLY_SPECIFIED'
  LEFT OUTER JOIN
  concept_reference_term_map_view crtv_answer ON  ca.answer_concept = crtv_answer.concept_id AND
                                                  crtv_answer.concept_map_type_name = 'SAME-AS' AND
                                                  crtv_answer.concept_reference_source_name = 'EndTB-Export'
ORDER BY  question ASC,
  question_code ASC,
  sort_weight ASC;


