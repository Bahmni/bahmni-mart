package org.bahmni.mart.config.procedure;

import org.bahmni.mart.config.view.ViewExecutor;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
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

    @Autowired
    private FreeMarkerEvaluator<Object> freeMarkerEvaluator;

    private List<String> listOfFailedProcedure = new ArrayList<>();

    public void execute(List<ProcedureDefinition> procedureDefinitions) {
        procedureDefinitions.forEach(procedureDefinition -> {
            try {
                logger.info(String.format("Executing the procedure '%s'.", procedureDefinition.getName()));
                martJdbcTemplate.execute(getUpdatedProcedureSQL(procedureDefinition.getSourceFilePath(),
                        procedureDefinition.getProcedureParameters()));
            } catch (Exception e) {
                listOfFailedProcedure.add(procedureDefinition.getName());
                logger.error(String.format("Unable to execute the procedure %s", procedureDefinition.getName()), e);
            }
        });
    }

    public List<String> getFailedProcedures() {
        return listOfFailedProcedure;
    }

    private String getUpdatedProcedureSQL(String sourceFilePath, ProcedureParameters procedureParameters)
            throws SQLException {
        if (isNotEmpty(sourceFilePath)) {
            String sqlFromFile =  getProcedureSql(sourceFilePath, procedureParameters);
            if (isValid(sqlFromFile)) {
                return sqlFromFile;
            } else {
                throw new SQLException();
            }
        }
        return "";
    }

    private String getProcedureSql(String sourceFilePath, ProcedureParameters procedureParameters) {
        String sqlFromFile = "";
        if (sourceFilePath.endsWith(".sql")) {
            sqlFromFile = getSqlFromFile(sourceFilePath);
        } else if (sourceFilePath.endsWith(".ftl")) {
            sqlFromFile = freeMarkerEvaluator.evaluate(sourceFilePath, procedureParameters);
            return sqlFromFile;
        }
        return sqlFromFile;
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
