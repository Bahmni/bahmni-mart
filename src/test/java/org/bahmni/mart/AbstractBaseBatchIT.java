package org.bahmni.mart;

import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:test.properties")
public abstract class AbstractBaseBatchIT {
    @Qualifier("postgresJdbcTemplate")
    @Autowired
    protected JdbcTemplate postgresJdbcTemplate;

    @After
    public void tearDown() throws Exception {
        postgresJdbcTemplate.execute("DROP SCHEMA PUBLIC CASCADE");
    }
}
