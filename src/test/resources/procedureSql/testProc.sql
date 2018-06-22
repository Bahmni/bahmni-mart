CREATE OR REPLACE FUNCTION getAllergyStatus(patient INTEGER)
RETURNS TEXT AS $$
DECLARE allergyStatus TEXT;
BEGIN
SELECT allergy_status INTO allergyStatus FROM patient_allergy_status_test_default WHERE patient_id = patient;
RETURN allergyStatus;
END;
$$  LANGUAGE plpgsql;