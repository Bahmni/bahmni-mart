package org.bahmni.mart.table;

import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableGeneratorStep {
    @Qualifier("martJdbcTemplate")
    @Autowired
    private JdbcTemplate martJdbcTemplate;

    @Autowired
    private FreeMarkerEvaluator<TableData> freeMarkerEvaluatorForTables;

    public void createTables(List<TableData> tables) {
        tables.forEach(tableData -> {
            SpecialCharacterResolver.resolveTableData(tableData);
            String sql = freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableData);
            martJdbcTemplate.execute(sql);
            }
        );
    }
}
