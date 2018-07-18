package org.bahmni.mart.config.procedure;

import org.bahmni.mart.config.view.ViewExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;
import static org.bahmni.mart.config.job.SQLFileLoader.loadResource;

@Component
public class ProcedureExecutor {

    private static Logger logger = LoggerFactory.getLogger(ViewExecutor.class);

    @Qualifier("martJdbcTemplate")
    @Autowired
    private JdbcTemplate martJdbcTemplate;

    private List<String> listOfFailedProcedure = new ArrayList<>();

    public void execute(List<ProcedureDefinition> procedureDefinitions) {
        procedureDefinitions.forEach(procedureDefinition -> {
            try {
                logger.info(String.format("Executing the procedure '%s'.", procedureDefinition.getName()));
                martJdbcTemplate.execute(getUpdatedProcedureSQL(procedureDefinition.getSourceFilePath()));
            } catch (Exception e) {
                listOfFailedProcedure.add(procedureDefinition.getName());
                logger.error(String.format("Unable to execute the procedure %s", procedureDefinition.getName()), e);
            }
        });
    }

    public List<String> getFailedProcedures() {
        return listOfFailedProcedure;
    }

    private String getUpdatedProcedureSQL(String sourceFilePath) throws SQLException {
        String sqlFromFile = getSqlFromFile(sourceFilePath);
        if (isValid(sqlFromFile)) {
            return sqlFromFile;
        }
        throw new SQLException();
    }

    private boolean isValid(String sql) {
        return !sql.toUpperCase().contains("DROP");
    }

    private String getSqlFromFile(String sourceFilePath) {
        if (isNotEmpty(sourceFilePath)) {
            Resource readerSqlResource = loadResource(sourceFilePath);
            return convertResourceOutputToString(readerSqlResource);
        }
        return "";
    }
}
