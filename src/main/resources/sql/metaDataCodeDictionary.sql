SELECT  CONCAT('\"', cv.concept_full_name, '\"')  AS fully_specified_name,
        CONCAT('\"', cv.concept_short_name, '\"') AS question,
        cv.concept_datatype_name,
        CONCAT( '\"',
                REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(cv.description, '"', ' '), '\t', ' '), '\n', ' '), '\r', ' '), ',', ' '),
                '\"')        AS description,
        crtv.code                                 AS question_header,
        CONCAT('\"', cn.name, '\"')               AS answer,
        crtv_answer.code                          AS answer_code
FROM    concept_view cv
  INNER JOIN
  concept_reference_term_map_view crtv ON   cv.concept_id = crtv.concept_id AND
                                            crtv.concept_reference_source_name = 'EndTB-Export' AND
                                            crtv.concept_map_type_name = 'SAME-AS' AND cv.concept_datatype_name IN ('Numeric', 'Text', 'Date', 'Boolean', 'Coded')
  LEFT JOIN
  concept_answer ca ON cv.concept_id = ca.concept_id
  LEFT OUTER JOIN
  concept_name cn ON  ca.answer_concept = cn.concept_id AND
                      cn.concept_name_type = 'FULLY_SPECIFIED'
  LEFT OUTER JOIN
  concept_reference_term_map_view crtv_answer ON  ca.answer_concept = crtv_answer.concept_id AND
                                                  crtv_answer.concept_map_type_name = 'SAME-AS' AND
                                                  crtv_answer.concept_reference_source_name = 'EndTB-Export'


  UNION
  select CONCAT('\"', codes.`Non TB Drug frequency`, '\"'), "", "Coded", "", codes.`othdrugfreq`, CONCAT('\"',cv.concept_full_name, '\"'),  crtmv.code
  from concept  c
  join concept_view cv ON cv.concept_id = c.concept_id AND c.class_id=20
  JOIN (SELECT "Non TB Drug frequency", "othdrugfreq" UNION SELECT "TB Drug Frequency","tbdrugfreq" ) as codes
  JOIN concept_reference_term_map_view crtmv ON crtmv.concept_id= c.concept_id AND crtmv.concept_map_type_name = 'SAME-AS' AND crtmv.concept_reference_source_name = 'EndTB-Export'

  UNION
  SELECT CONCAT('\"',cv.concept_full_name, '\"'), CONCAT('\"',cv.concept_short_name,'\"'), "Coded", "",
  IF(cv.concept_full_name = 'All TB Drugs', "tbdrug",
        IF(cv.concept_full_name='Dosing Instructions',"reas_othdrug",null)),
  CONCAT('\"',tbDrugs.concept_full_name, '\"'), crtmv.code
  FROM concept c JOIN concept_view cv ON cv.concept_full_name IN ('All TB Drugs','Dosing Instructions')
                                       AND c.concept_id = cv.concept_id
  JOIN concept_set cs ON cs.concept_set = c.concept_id
  JOIN concept_view tbDrugs ON tbDrugs.concept_id = cs.concept_id
  JOIN concept_reference_term_map_view crtmv ON crtmv.concept_id= tbDrugs.concept_id  AND
                                                crtmv.concept_map_type_name = 'SAME-AS' AND  crtmv.concept_reference_source_name = 'EndTB-Export'
UNION
SELECT CONCAT('\"',cv.concept_full_name, '\"'), CONCAT('\"',cv.concept_short_name,'\"'), "Coded", "",
   codesForDosingUnits.othdrugform,
  CONCAT('\"',tbDrugs.concept_full_name, '\"'), crtmv.code
FROM concept c JOIN concept_view cv ON cv.concept_full_name IN ('Dosing Units')
                                       AND c.concept_id = cv.concept_id
  JOIN concept_set cs ON cs.concept_set = c.concept_id
  JOIN (SELECT "othdrugform" UNION SELECT "othtotdrugform" UNION SELECT "unit") as codesForDosingUnits
  JOIN concept_view tbDrugs ON tbDrugs.concept_id = cs.concept_id
  JOIN concept_reference_term_map_view crtmv ON crtmv.concept_id= tbDrugs.concept_id  AND
                                                crtmv.concept_map_type_name = 'SAME-AS' AND  crtmv.concept_reference_source_name = 'EndTB-Export'
UNION
SELECT CONCAT('\"',cv.concept_full_name, '\"'), CONCAT('\"',cv.concept_short_name,'\"'), "Coded", "",
  codesForRoutes.othdrugroute,
  CONCAT('\"',tbDrugs.concept_full_name, '\"'), crtmv.code
FROM concept c JOIN concept_view cv ON cv.concept_full_name IN ('Drug Routes')
                                       AND c.concept_id = cv.concept_id
  JOIN concept_set cs ON cs.concept_set = c.concept_id
  JOIN (SELECT "othdrugroute" UNION SELECT "tbroute") as codesForRoutes
  JOIN concept_view tbDrugs ON tbDrugs.concept_id = cs.concept_id
  JOIN concept_reference_term_map_view crtmv ON crtmv.concept_id= tbDrugs.concept_id  AND
                                                crtmv.concept_map_type_name = 'SAME-AS' AND  crtmv.concept_reference_source_name = 'EndTB-Export'

  UNION
  SELECT CONCAT('\"',"Non TB Drugs Notes", '\"'),"", "Text","", CONCAT('\"',"additional_instructions",'\"'), "",""
  UNION
  SELECT CONCAT('\"',"Additional Instructions", '\"'),"", "Text", "", CONCAT('\"',"addlinstr",'\"'), "",""
  UNION
  SELECT CONCAT('\"',"Non TB Drug Start Date", '\"'),"", "Date", "", CONCAT('\"',"d_othdrugstart",'\"'), "",""
  UNION
  SELECT CONCAT('\"',"Non TB Drug End Date", '\"'),"", "Date", "", CONCAT('\"',"d_othdrugend",'\"'), "",""
  UNION
  SELECT CONCAT('\"',"Treatment Registration Date", '\"'),"", "Date", "", CONCAT('\"',"d_reg",'\"'), "",""
  UNION
  SELECT CONCAT('\"',"TB Drug Start Date", '\"'),"", "Date", "", CONCAT('\"',"d_tbdrugstart",'\"'), "",""
  UNION
  SELECT CONCAT('\"',"TB Drug End Date", '\"'),"", "Date", "", CONCAT('\"',"d_tbdrugend",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Non TB Drug Start Date", '\"'),"", "Date", "", CONCAT('\"',"d_othdrugstart",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Non TB Drug End Date", '\"'),"", "Date", "", CONCAT('\"',"d_othdrugend",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Age", '\"'),"", "Numeric", "",  CONCAT('\"',"age",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Non TB Drug Duration", '\"'),"", "Text", "", CONCAT('\"',"duration",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Non TB Drugs", '\"'),"", "Text", "", CONCAT('\"',"othdrug",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Non TB Drug Dose", '\"'),"", "Numeric", "", CONCAT('\"',"othdrugdose",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Non TB Drug Quantity", '\"'),"", "Numeric", "", CONCAT('\"',"othtotdrugqty",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Registration Facility", '\"'),"", "Text", "", CONCAT('\"',"reg_facility",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Registration Number", '\"'),"", "Text", "", CONCAT('\"',"regnum",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Sex", '\"'),"", "Coded", "", CONCAT('\"',"sex",'\"'),"Male","1"
  UNION
  SELECT CONCAT('\"',"Sex", '\"'),"", "Coded", "", CONCAT('\"',"sex",'\"'),"Female","2"
  UNION
  SELECT CONCAT('\"',"Sex", '\"'),"", "Coded", "", CONCAT('\"',"sex",'\"'),"Other","3"
  UNION
  SELECT CONCAT('\"',"Status", '\"'),"", "Text", "", CONCAT('\"',"status",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"TB Drug dose", '\"'),"", "Numeric", "", CONCAT('\"',"tbdose",'\"'),"",""
  UNION
  SELECT CONCAT('\"',"Treatment Registration type", '\"'),"", "Text", "", CONCAT('\"',"tbregtype",'\"'),"",""

  ORDER BY  fully_specified_name ASC,
  question ASC,
  question_header ASC;


