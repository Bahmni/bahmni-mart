package org.bahmni.mart.table;

import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.helper.IncrementalUpdater;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bahmni.mart.table.SpecialCharacterResolver.resolveTableData;

@Component
public class TableGeneratorStep {
    @Qualifier("martJdbcTemplate")
    @Autowired
    private JdbcTemplate martJdbcTemplate;

    @Autowired
    private FreeMarkerEvaluator<TableData> freeMarkerEvaluatorForTables;

    @Autowired
    private IncrementalUpdater incrementalUpdater;

    public void createTables(List<TableData> tables) {
        tables.forEach(tableData -> {
            SpecialCharacterResolver.resolveTableData(tableData);
            String sql = freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableData);
            martJdbcTemplate.execute(sql);
            }
        );
    }

    //This method can replace the previous createTables method once incremental update is implemented for all jobs
    public void createTablesForObs(List<TableData> tables) {
        tables.forEach(tableData -> {
                resolveTableData(tableData);
                String actualTableName = SpecialCharacterResolver.getActualTableName(tableData.getName());
                if (incrementalUpdater.isMetaDataChanged(actualTableName)) {
                    String sql = freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableData);
                    martJdbcTemplate.execute(sql);
                }
            }
        );
    }
}
