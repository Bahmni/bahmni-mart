package org.bahmni.mart.config.job;

import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

@Component
public class CustomCodesTasklet implements Tasklet {

    @Autowired
    @Qualifier("martJdbcTemplate")
    private JdbcTemplate martJdbcTemplate;

    @Autowired
    @Qualifier("martCopyManager")
    private CopyManager copyManager;

    private static final Logger logger = LoggerFactory.getLogger(CustomCodesTasklet.class);

    private String readerFilePath;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
        createTable();
        importData();
        return RepeatStatus.FINISHED;
    }

    private void importData() {
        try {
            copyManager.copyIn("COPY custom_codes FROM STDIN DELIMITER ',' HEADER CSV",
                    new FileInputStream(readerFilePath));
        } catch (DataAccessResourceFailureException | DataIntegrityViolationException |
                SQLException | IOException exception) {
            logger.error(exception.getMessage(), exception);
        }
    }

    private void createTable() {
        String tableCreationSql = "DROP TABLE IF EXISTS custom_codes; " +
                "CREATE TABLE custom_codes ( name TEXT NOT NULL, source TEXT NOT NULL ,type TEXT ,code TEXT," +
                "UNIQUE (name, type ,source));";
        martJdbcTemplate.execute(tableCreationSql);
    }

    public void setReaderFilePath(String readerFilePath) {
        this.readerFilePath = readerFilePath;
    }
}
