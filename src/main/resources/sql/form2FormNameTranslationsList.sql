SELECT
       form.name,
       fr.value_reference
FROM form
       INNER JOIN form_resource fr ON form.form_id = fr.form_id and fr.name like '%FormName_Translation'
       INNER JOIN (select
                          name,
                          MAX(version) as version
                   from form
                   where published = true
                   group by name) as MaxForm
         on form.name = MaxForm.name and form.version = MaxForm.version
WHERE form.retired = FALSE AND form.published = TRUE;