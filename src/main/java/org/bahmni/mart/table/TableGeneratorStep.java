package org.bahmni.mart.table;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.exports.updatestrategy.IncrementalUpdateStrategy;
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

    @Autowired
    private IncrementalStrategyContext incrementalStrategyContext;

    public void createTables(List<TableData> tables, JobDefinition jobDefinition) {
        IncrementalUpdateStrategy updateStrategy = incrementalStrategyContext.getStrategy(jobDefinition.getType());
        tables.forEach(tableData -> {
                SpecialCharacterResolver.resolveTableData(tableData);
                String actualTableName = SpecialCharacterResolver.getActualTableName(tableData.getName());
                if (updateStrategy.isMetaDataChanged(actualTableName, jobDefinition.getName())) {
                    String sql = freeMarkerEvaluatorForTables.evaluate("ddlForForm.ftl", tableData);
                    martJdbcTemplate.execute(sql);
                }
            }
        );
    }
}
