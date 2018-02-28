package org.bahmni.mart.table;

import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableGeneratorStep {

    @Qualifier("postgresJdbcTemplate")
    @Autowired
    private JdbcTemplate postgresJdbcTemplate;

    @Autowired
    private FreeMarkerEvaluator<TableData> freeMarkerEvaluatorForTables;

    private static final Logger log = LoggerFactory.getLogger(TableGeneratorStep.class);

    public void createTables(List<TableData> tables) {
        tables.forEach(
            tableData -> {
                try {
                    String sql = freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableData);
                    postgresJdbcTemplate.execute(sql);
                } catch (Exception e) {
                    log.error("Cannot create table: " + tableData.getName() + " " + e.getMessage());
                }
            }
        );
    }
}
