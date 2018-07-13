package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.exception.InvalidOrderTypeException;
import org.bahmni.mart.exception.NoSamplesFoundException;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.bahmni.mart.helper.OrderConceptUtil;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.exports.writer.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.bahmni.mart.BatchUtils.constructSqlWithParameter;
import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;

@Component
public class OrderStepConfigurer implements StepConfigurerContract {

    private static final Logger logger = LoggerFactory.getLogger(OrderStepConfigurer.class);

    private static final String ALL_ORDERABLES = "All Orderables";
    private static final int PAGE_SIZE = 200000;

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
    private ConceptService conceptService;

    @Autowired
    private OrderConceptUtil orderConceptUtil;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Value("classpath:sql/orders.sql")
    private Resource ordersSQLResource;

    private List<String> orderableConceptNames;

    private List<TableData> orderablesTableData;

    @Override
    public void generateTableData(JobDefinition jobDefinition) {
        orderablesTableData = getOrderablesTableData(getOrderables());
    }

    @Override
    public void createTables(JobDefinition jobDefinition) {
        tableGeneratorStep.createTables(orderablesTableData, jobDefinition);
    }

    private List<String> getOrderables() {
        if (orderableConceptNames == null) {
            JobDefinition ordersJobDefinition = jobDefinitionReader.getJobDefinitionByName("Orders Data");
            orderableConceptNames = conceptService.getChildConcepts(ALL_ORDERABLES,
                    ordersJobDefinition.getLocale()).stream()
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
        JdbcPagingItemReader<Map<String, Object>> reader = new JdbcPagingItemReader<>();
        reader.setQueryProvider(getMySqlPagingQueryProvider(orderable));
        reader.setSaveState(true);
        reader.setDataSource(dataSource);
        reader.setPageSize(PAGE_SIZE);
        reader.setRowMapper(new ColumnMapRowMapper());
        try {
            reader.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return reader;
    }


    private MySqlPagingQueryProvider getMySqlPagingQueryProvider(String orderable)
            throws InvalidOrderTypeException, NoSamplesFoundException {
        MySqlPagingQueryProvider mySqlPagingQueryProvider = new MySqlPagingQueryProvider();
        mySqlPagingQueryProvider.setSelectClause("SELECT * ");
        mySqlPagingQueryProvider.setFromClause(String
                .format("FROM (%s) AS mergedOrders", getOrderReaderSQL(orderable)));
        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("patient_id", Order.DESCENDING);
        mySqlPagingQueryProvider.setSortKeys(sortKey);
        return mySqlPagingQueryProvider;

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
