SELECT encounter_id, obs_id
FROM (SELECT encounter_id, max(obs_id) AS obs_id
      FROM (SELECT encounter_id, obs_id, form_namespace_and_path FROM obs WHERE form_namespace_and_path IS NOT NULL) res
      WHERE form_namespace_and_path LIKE CONCAT('%', :formName, '.%')
      GROUP BY encounter_id) AS o
