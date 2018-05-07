package org.bahmni.mart.config.job;

import org.apache.commons.lang3.StringUtils;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Component
public class CustomCodesTasklet implements Tasklet {

    @Autowired
    @Qualifier("martJdbcTemplate")
    private JdbcTemplate martJdbcTemplate;

    @Autowired
    @Qualifier("martCopyManager")
    private CopyManager copyManager;

    private static final Logger logger = LoggerFactory.getLogger(CustomCodesTasklet.class);

    private String sourceFilePath;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
        createTable();
        importData();
        return RepeatStatus.FINISHED;
    }

    private void importData() {
        try {
            List<String> headers = getHeaders(sourceFilePath);
            if (isValid(headers)) {
                copyManager.copyIn(String.format("COPY custom_codes (%s) FROM STDIN DELIMITER ',' CSV HEADER",
                        StringUtils.join(headers, ",")), new FileInputStream(sourceFilePath));
            }
        } catch (DataAccessResourceFailureException | DataIntegrityViolationException |
                SQLException | IOException exception) {
            logger.error(exception.getMessage(), exception);
        }
    }

    private List<String> getHeaders(String sourceFilePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileInputStream(sourceFilePath));
        String[] headers = scanner.nextLine().split(",");
        scanner.close();
        return Arrays.asList(headers);
    }

    private boolean isValid(List<String> headers) {
        List<String> columnsList = Arrays.asList("name", "source", "type", "code");
        if (columnsList.containsAll(headers)) {
            return true;
        }
        throw new DataIntegrityViolationException("Invalid headers");
    }

    private void createTable() {
        String tableCreationSql = "DROP TABLE IF EXISTS custom_codes; " +
                "CREATE TABLE custom_codes ( name TEXT NOT NULL, source TEXT NOT NULL ,type TEXT ,code TEXT," +
                "UNIQUE (name, type ,source));";
        martJdbcTemplate.execute(tableCreationSql);
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }
}
