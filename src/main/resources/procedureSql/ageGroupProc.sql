CREATE OR REPLACE FUNCTION age_group(process_start_date TIMESTAMP WITHOUT TIME ZONE,
                                     birthdate          TIMESTAMP WITHOUT TIME ZONE)
  RETURNS CHARACTER VARYING
LANGUAGE plpgsql
AS $$
BEGIN
  IF EXTRACT(YEAR FROM (age(process_start_date, birthdate))) BETWEEN 0 AND 5
  THEN RETURN '0-5';
  ELSEIF EXTRACT(YEAR FROM (age(process_start_date, birthdate))) BETWEEN 6 AND 14
    THEN RETURN '05-14';
  ELSEIF (EXTRACT(YEAR FROM (age(process_start_date, birthdate))) BETWEEN 15 AND 24)
    THEN RETURN '15-24';
  ELSEIF EXTRACT(YEAR FROM (age(process_start_date, birthdate))) BETWEEN 25 AND 34
    THEN RETURN '25-34';
  ELSEIF EXTRACT(YEAR FROM (age(process_start_date, birthdate))) BETWEEN 35 AND 44
    THEN RETURN '35-44';
  ELSEIF EXTRACT(YEAR FROM (age(process_start_date, birthdate))) BETWEEN 45 AND 54
    THEN RETURN '45-54';
  ELSEIF EXTRACT(YEAR FROM (age(process_start_date, birthdate))) BETWEEN 55 AND 64
    THEN RETURN '55-64';
  ELSEIF EXTRACT(YEAR FROM (age(process_start_date, birthdate))) > 64
    THEN RETURN '65+';
  ELSE RETURN NULL;
  END IF;

END;
$$;