package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.exception.InvalidOrderTypeException;
import org.bahmni.mart.exception.NoSamplesFoundException;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.bahmni.mart.helper.OrderConceptUtil;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.BatchUtils.constructSqlWithParameter;
import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;

@Component
public class OrderStepConfigurer implements StepConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(OrderStepConfigurer.class);

    private static final String ALL_ORDERABLES = "All Orderables";

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Autowired
    private TableDataGenerator tableDataGenerator;

    @Autowired
    private ObsService obsService;

    @Autowired
    private OrderConceptUtil orderConceptUtil;

    @Value("classpath:sql/orders.sql")
    private Resource ordersSQLResource;

    @Override
    public void createTables() {
        List<String> orderableNames = obsService.getChildConcepts(ALL_ORDERABLES).stream()
                .map(Concept::getName).collect(Collectors.toList());

        tableGeneratorStep.createTables(getOrderablesTableData(orderableNames));
    }

    private List<TableData> getOrderablesTableData(List<String> orderableNames) {
        List<TableData> tableData = new ArrayList<>();

        orderableNames.forEach(orderable -> {
            try {
                int orderTypeId = orderConceptUtil.getOrderTypeId(orderable);
                String orderSQL = convertResourceOutputToString(ordersSQLResource);
                orderSQL = constructSqlWithParameter(orderSQL, "orderTypeId", Integer.toString(orderTypeId));

                tableData.add(tableDataGenerator.getTableData(orderable, orderSQL));
            } catch (NoSamplesFoundException | InvalidOrderTypeException e) {
                logger.info(e.getMessage());
            } catch (Exception e) {
                logger.error(String
                        .format("An error occurred while retrieving table data for orderable %s", orderable), e);
            }
        });

        return tableData;
    }

    @Override
    public void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport, JobDefinition jobDefinition) {

    }
}
