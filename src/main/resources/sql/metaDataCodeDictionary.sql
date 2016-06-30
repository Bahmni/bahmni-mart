select CONCAT('\"',cv.concept_full_name,'\"') as fully_specified_name,CONCAT('\"',cv.concept_short_name,'\"') as question,CONCAT('\"',cv.description,'\"') as description, crtv.code as question_header, CONCAT('\"',cn.name,'\"') as answer,crtv_answer.code as answer_code
from concept_view cv
  inner join concept_reference_term_map_view crtv on cv.concept_id=crtv.concept_id and crtv.concept_reference_source_name = 'EndTB-Export' and crtv.concept_map_type_name='SAME-AS'
  JOIN concept_answer ca on cv.concept_id=ca.concept_id
  LEFT OUTER JOIN concept_name cn on ca.answer_concept=cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED'
  LEFT OUTER JOIN concept_reference_term_map_view  crtv_answer on ca.answer_concept = crtv_answer.concept_id and crtv_answer.concept_map_type_name='SAME-AS' and crtv_answer.concept_reference_source_name='EndTB-Export'
ORDER BY question asc,question_header asc,sort_weight asc;
