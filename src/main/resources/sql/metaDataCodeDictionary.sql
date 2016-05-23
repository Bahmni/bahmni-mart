select cv.concept_full_name, crtv.code
from concept_view cv,concept_reference_term_map_view crtv
where cv.concept_id = crtv.concept_id
and crtv.concept_reference_source_name = 'EndTB-Export'
and crtv.concept_map_type_name='SAME-AS';
