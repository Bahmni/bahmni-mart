select
  pd.person_id AS patient_id,
  gender,
  birthdate,
  birthtime,
  birthdate_estimated,
  dead,
  death_date,
  deathdate_estimated,
  cause_of_death,
  pat.*
from person_details_default pd LEFT OUTER JOIN person_attributes pat on pat.person_id = pd.person_id

