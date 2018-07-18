package org.bahmni.mart;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles(profiles = "test", resolver = SystemPropertyActiveProfileResolver.class)
public abstract class AbstractBaseBatchIT {
    @Qualifier("martJdbcTemplate")
    @Autowired
    protected JdbcTemplate martJdbcTemplate;

    @Before
    public void setUp() throws Exception {
        martJdbcTemplate.execute("DROP SCHEMA IF EXISTS PUBLIC CASCADE; CREATE SCHEMA PUBLIC;");
        addMarkersInfo();
    }

    @After
    public void tearDown() throws Exception {
        martJdbcTemplate.execute("DROP SCHEMA IF EXISTS PUBLIC CASCADE; CREATE SCHEMA PUBLIC;");
    }

    private void addMarkersInfo() {
        martJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS markers (\n" +
                "                job_name          TEXT PRIMARY KEY,\n" +
                "                event_record_id   INTEGER NOT NULL,\n" +
                "                category          TEXT NOT NULL,\n" +
                "                table_name        TEXT NOT NULL\n);");

        martJdbcTemplate.execute("INSERT INTO markers VALUES('Obs Data', 0, 'Encounter', 'encounter');");

        martJdbcTemplate.execute(
                "CREATE TABLE if NOT EXISTS patient_program_data_default (\n" +
                        "                   patient_program_id INTEGER,\n" +
                        "                   patient_id INTEGER\n);");
        martJdbcTemplate.execute("INSERT INTO patient_program_data_default VALUES (1, 124);");
        martJdbcTemplate.execute("INSERT INTO patient_program_data_default VALUES (2, 125);");
        martJdbcTemplate.execute("INSERT INTO markers VALUES('Patient program data', 1, 'programenrollment', " +
                                                                "'patient_program');");
    }


}
