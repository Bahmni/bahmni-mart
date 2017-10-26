SELECT
     CONCAT("\"", questionConcept.concept_full_name, "\"")                                  AS `Fully Specified Name`,
     CONCAT("\"", questionConcept.concept_short_name, "\"")                                 AS `Question`,
     questionConcept.concept_datatype_name                                                  AS `Qeustion Datatype`,
     CONCAT("\"", questionConcept.description, "\"")                                        AS `Description`,
     crtv.code                                                                              AS `Question Header`,
     CONCAT("\"",answerConcept.answer, "\"")                                                AS `Answer`,
     crtvForAnswerCode.code                                                                 AS `Answer Code`
  FROM concept_set cs
    LEFT JOIN (SELECT
                    ca.concept_id,
                    cv.concept_full_name                      AS `answer`,
                    ca.answer_concept
              FROM concept_answer ca
              INNER JOIN concept_view cv ON cv.concept_id = ca.answer_concept) answerConcept ON answerConcept.concept_id = cs.concept_id
    INNER JOIN concept_view questionConcept ON questionConcept.concept_id = cs.concept_id AND questionConcept.concept_short_name IS NOT NULL
    LEFT JOIN concept_reference_term_map_view crtvForAnswerCode ON crtvForAnswerCode.concept_id = answerConcept.answer_concept AND crtvForAnswerCode.concept_reference_source_name = 'MSF-INTERNAL'
    LEFT JOIN concept_reference_term_map_view crtv ON  cs.concept_id = crtv.concept_id AND crtv.concept_map_type_name = 'SAME-AS' AND crtv.concept_reference_source_name = 'MSF-INTERNAL'
UNION
SELECT
      "Drug Code"                                                         AS `Fully Specified Name`,
      "drug_code"                                                         AS `question`,
      drugCV.concept_class_name                                           AS `question_datatype`,
      concat("\"", drugCV.description, "\"")                              AS `description`,
      "drug_code"                                                         AS `header`,
      concat("\"", drugCV.concept_full_name, "\"")                        AS `answer`,
      drugCrtv.code                                                       AS `answer code`
    FROM concept_view drugCV
    INNER JOIN concept_reference_term_map_view drugCrtv ON drugCrtv.concept_id = drugCV.concept_id
                                            AND concept_reference_source_name = 'MSF-INTERNAL' AND drugCV.concept_class_name = 'Drug'
UNION
SELECT CONCAT('\"',"Patient Identifier", '\"'),"", "Text","", CONCAT('\"',"patient_id",'\"'), "",""
UNION
SELECT CONCAT('\"',"Program Identifier", '\"'),"", "Text","", CONCAT('\"',"patient_prg_id",'\"'), "",""
UNION
SELECT CONCAT('\"',"Drug Name", '\"'),"", "Text","", CONCAT('\"',"drug_name",'\"'), "",""
UNION
SELECT CONCAT('\"',"Drug Start Date", '\"'),"", "Date", "", CONCAT('\"',"drug_start",'\"'), "",""
UNION
SELECT CONCAT('\"',"Drug End Date", '\"'),"", "Date", "", CONCAT('\"',"drug_stop",'\"'), "",""
UNION
SELECT CONCAT('\"',"Drug dose", '\"'),"", "Numeric", "", CONCAT('\"',"dose",'\"'),"",""
UNION
SELECT CONCAT('\"',"Additional Instructions", '\"'),"", "Text", "", CONCAT('\"',"additional_instr",'\"'), "",""
UNION
SELECT CONCAT('\"',"Location Name", '\"'),"", "Text", "", CONCAT('\"',"location",'\"'), "",""
UNION
SELECT CONCAT('\"',"Drug Duration", '\"'),"", "Numeric", "", CONCAT('\"',"duration",'\"'), "",""
UNION
SELECT CONCAT('\"',"Direct Observation Therapy", '\"'),"", "Coded", "", CONCAT('\"',"dot",'\"'), "Yes",""
UNION
SELECT CONCAT('\"',"Direct Observation Therapy", '\"'),"", "Coded", "", CONCAT('\"',"dot",'\"'), "No",""
UNION
SELECT CONCAT('\"',"Drug Dispense", '\"'),"", "Coded", "", CONCAT('\"',"dispense",'\"'), "Yes",""
UNION
SELECT CONCAT('\"',"Drug Dispense", '\"'),"", "Coded", "", CONCAT('\"',"dispense",'\"'), "No",""
UNION
SELECT CONCAT('\"',"Drug Stop Reason", '\"'),"", "Text", "", CONCAT('\"',"stop_reason",'\"'), "",""
UNION
SELECT CONCAT('\"',"Drug Stop Notes", '\"'),"", "Text", "", CONCAT('\"',"stop_notes",'\"'), "",""
UNION
SELECT CONCAT('\"',"Date Of Birth", '\"'),"", "Date", "", CONCAT('\"',"dob",'\"'), "",""
UNION
SELECT CONCAT('\"',"Patient Age", '\"'),"", "Number", "", CONCAT('\"',"age",'\"'), "",""
UNION
SELECT CONCAT('\"',"Sex", '\"'),"", "Coded", "", CONCAT('\"',"sex",'\"'),"Male","1"
UNION
SELECT CONCAT('\"',"Sex", '\"'),"", "Coded", "", CONCAT('\"',"sex",'\"'),"Female","2"
UNION
SELECT CONCAT('\"',"Sex", '\"'),"", "Coded", "", CONCAT('\"',"sex",'\"'),"Other","3"
UNION
SELECT CONCAT('\"',"Program Name", '\"'),"", "Text", "", CONCAT('\"',"prg_name",'\"'),"",""
UNION
SELECT CONCAT('\"',"Program Start Date", '\"'),"", "Date", "", CONCAT('\"',"prg_start_dt",'\"'),"",""
UNION
SELECT CONCAT('\"',"Program End Date", '\"'),"", "Date", "", CONCAT('\"',"prg_end_dt",'\"'),"",""
UNION
SELECT CONCAT('\"',"Program Outcome", '\"'),"", "Text", "", CONCAT('\"',"prg_outcome",'\"'),"",""
UNION
SELECT CONCAT('\"',"Program State Start Date", '\"'),"", "Date", "", CONCAT('\"',"prg_state_start",'\"'),"",""
UNION
SELECT CONCAT('\"',"Program State End Date", '\"'),"", "Date", "", CONCAT('\"',"prg_state_end",'\"'),"","";
