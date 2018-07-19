package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.OrderStepConfigurer;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.bahmni.mart.table.SpecialCharacterResolver.getActualTableName;

@Component
public class OrdersIncrementalUpdateStrategy extends AbstractIncrementalUpdateStrategy {

    @Autowired
    private OrderStepConfigurer orderStepConfigurer;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Override
    protected boolean getMetaDataChangeStatus(String actualTableName, String jobName) {
        JobDefinition ordersJobDefinition = jobDefinitionReader.getJobDefinitionByName(jobName);
        if (Objects.isNull(ordersJobDefinition) || Objects.isNull(ordersJobDefinition.getIncrementalUpdateConfig())) {
            return true;
        }

        TableData currentTableData = orderStepConfigurer.getTableData(getActualTableName(actualTableName));
        TableData existingTableData = getExistingTableData(actualTableName);
        return !currentTableData.equals(existingTableData);
    }
}
