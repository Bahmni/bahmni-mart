select o.obs_id,o.value_text
from obs o
where obs_id in (:childObsIds) and concept_id in (:leafConceptIds)
