SELECT
     CONCAT("\"", questionConcept.concept_full_name, "\"")                                  AS `fully_specified_name`,
     CONCAT("\"", questionConcept.concept_short_name, "\"")                                 AS `question`,
     questionConcept.concept_datatype_name                                                  AS `question_datatype`,
     CONCAT("\"", questionConcept.description, "\"")                                        AS `description`,
     crtv.code                                                                              AS `question_header`,
     CONCAT("\"",answerConcept.answer, "\"")                                                AS `answer`,
     crtvForAnswerCode.code                                                                 AS `answer_code`
  FROM concept_set cs
    LEFT JOIN (SELECT
                    ca.concept_id,
                    cv.concept_full_name                      AS `answer`,
                    ca.answer_concept
              FROM concept_answer ca
              INNER JOIN concept_view cv ON cv.concept_id = ca.answer_concept) answerConcept ON answerConcept.concept_id = cs.concept_id
    INNER JOIN concept_view questionConcept ON questionConcept.concept_id = cs.concept_id AND questionConcept.concept_short_name IS NOT NULL
    LEFT JOIN concept_reference_term_map_view crtvForAnswerCode ON crtvForAnswerCode.concept_id = answerConcept.answer_concept AND crtvForAnswerCode.concept_reference_source_name = :conceptReferenceSource
    LEFT JOIN concept_reference_term_map_view crtv ON  cs.concept_id = crtv.concept_id AND crtv.concept_map_type_name = 'SAME-AS' AND crtv.concept_reference_source_name = :conceptReferenceSource
UNION
SELECT
      "Drug Code"                                                         AS `Fully Specified Name`,
      "drug_code"                                                         AS `question`,
      "Drug"                                                              AS `question_datatype`,
      concat("\"", drugDesc.description, "\"")                              AS `description`,
      "drug_code"                                                         AS `header`,
      concat("\"", drugCV.name, "\"")                        AS `answer`,
      drugCrtv.code                                                       AS `answer code`
    FROM drug drugCV
    LEFT JOIN concept_description drugDesc on drugDesc.concept_id = drugCV.concept_id
    INNER JOIN concept_reference_term_map_view drugCrtv ON drugCrtv.concept_id = drugCV.concept_id
                                            AND concept_reference_source_name = :conceptReferenceSource