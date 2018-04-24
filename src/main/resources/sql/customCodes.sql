SELECT * FROM custom_codes
WHERE UPPER(source)=UPPER(:source) AND (UPPER(type) = UPPER(:type) OR type IS NULL );