SELECT form.form_id, form.name, form.version, f.value_reference FROM form
  INNER JOIN form_resource f ON form.form_id = f.form_id
  INNER JOIN (select name, MAX(version) as version from form where published =true group by name) as MaxForm
  on form.name = MaxForm.name and form.version = MaxForm.version
  WHERE form.retired = FALSE AND form.published = TRUE;
