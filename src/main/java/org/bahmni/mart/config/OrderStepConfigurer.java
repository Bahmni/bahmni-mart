package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.exception.InvalidOrderTypeException;
import org.bahmni.mart.exception.NoSamplesFoundException;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.bahmni.mart.helper.OrderConceptUtil;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.bahmni.mart.BatchUtils.constructSqlWithParameter;
import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;

@Component
public class OrderStepConfigurer implements StepConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(OrderStepConfigurer.class);

    private static final String ALL_ORDERABLES = "All Orderables";

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Autowired
    private TableDataGenerator tableDataGenerator;

    @Autowired
    private ObsService obsService;

    @Autowired
    private OrderConceptUtil orderConceptUtil;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Value("classpath:sql/orders.sql")
    private Resource ordersSQLResource;

    private List<String> orderableConceptNames;

    @Override
    public void createTables() {
        tableGeneratorStep.createTables(getOrderablesTableData(getOrderables()));
    }

    private List<String> getOrderables() {
        if (orderableConceptNames == null) {
            orderableConceptNames = obsService.getChildConcepts(ALL_ORDERABLES).stream()
                    .map(Concept::getName).collect(Collectors.toList());
        }
        return orderableConceptNames;
    }

    private List<TableData> getOrderablesTableData(List<String> orderableNames) {
        List<TableData> tableData = new ArrayList<>();

        orderableNames.forEach(orderable -> {
            try {
                String orderSQL = getOrderReaderSQL(orderable);

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

    private String getOrderReaderSQL(String orderable) throws NoSamplesFoundException, InvalidOrderTypeException {
        int orderTypeId = orderConceptUtil.getOrderTypeId(orderable);
        String orderSQL = convertResourceOutputToString(ordersSQLResource);
        orderSQL = constructSqlWithParameter(orderSQL, "orderTypeId", Integer.toString(orderTypeId));
        JobDefinition ordersJobDefinition = jobDefinitionReader.getJobDefinitionByName("Orders Data");
        return JobDefinitionUtil.getReaderSQLByIgnoringColumns(ordersJobDefinition.getColumnsToIgnore(), orderSQL);
    }

    @Override
    public void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport, JobDefinition jobDefinition) {
        for (String orderable : getOrderables()) {
            Optional<Step> optionalStep = getAnOrderableStep(orderable, jobDefinition);
            if (optionalStep.isPresent()) {
                completeDataExport.next(optionalStep.get());
            }
        }
    }

    private Optional<Step> getAnOrderableStep(String orderable, JobDefinition jobDefinition) {
        try {
            TaskletStep step = stepBuilderFactory.get(orderable)
                    .<Map<String, Object>, Map<String, Object>>chunk(jobDefinition.getChunkSizeToRead())
                    .reader(ordersDataReader(orderable))
                    .processor(orderDataProcessor(orderable))
                    .writer(ordersDataWriter(orderable))
                    .build();
            return Optional.of(step);
        } catch (InvalidOrderTypeException | NoSamplesFoundException e) {
            logger.info(e.getMessage());
        }
        return Optional.empty();
    }

    private ItemReader<? extends Map<String, Object>> ordersDataReader(String orderable)
            throws InvalidOrderTypeException, NoSamplesFoundException {
        String orderReaderSQL = getOrderReaderSQL(orderable);
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(orderReaderSQL);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    private TableDataProcessor orderDataProcessor(String orderable)
            throws InvalidOrderTypeException, NoSamplesFoundException {
        TableDataProcessor tableDataProcessor = new TableDataProcessor();
        tableDataProcessor.setTableData(getTableData(orderable));
        return tableDataProcessor;
    }

    private TableData getTableData(String orderable) throws NoSamplesFoundException, InvalidOrderTypeException {
        return tableDataGenerator.getTableData(orderable, getOrderReaderSQL(orderable));
    }

    private TableRecordWriter ordersDataWriter(String orderable)
            throws InvalidOrderTypeException, NoSamplesFoundException {
        TableRecordWriter writer = recordWriterObjectFactory.getObject();
        writer.setTableData(getTableData(orderable));
        return writer;
    }
}
