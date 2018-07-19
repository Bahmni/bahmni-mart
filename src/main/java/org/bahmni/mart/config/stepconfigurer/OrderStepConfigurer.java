package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exception.InvalidOrderTypeException;
import org.bahmni.mart.exception.NoSamplesFoundException;
import org.bahmni.mart.exports.updatestrategy.OrdersIncrementalUpdateStrategy;
import org.bahmni.mart.exports.writer.TableRecordWriter;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.bahmni.mart.helper.OrderConceptUtil;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.table.TableGeneratorStep;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.bahmni.mart.BatchUtils.constructSqlWithParameter;
import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQLByIgnoringColumns;
import static org.bahmni.mart.table.FormTableMetadataGenerator.getProcessedName;

@Component
public class OrderStepConfigurer implements StepConfigurerContract {

    private static final Logger logger = LoggerFactory.getLogger(OrderStepConfigurer.class);

    private static final String ALL_ORDERABLES = "All Orderables";
    private static final String ORDERS_JOB_NAME = "Orders Data";
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

    @Autowired
    private OrdersIncrementalUpdateStrategy ordersIncrementalUpdateStrategy;

    @Value("classpath:sql/orders.sql")
    private Resource ordersSQLResource;

    private List<String> orderableConceptNames;

    private Map<String, TableData> orderablesTableData = new HashMap<>();

    @Override
    public void generateTableData(JobDefinition jobDefinition) {
        List<String> orderables = getOrderables();
        generateOrderablesTableData(orderables);
    }

    @Override
    public void createTables(JobDefinition jobDefinition) {
        List<TableData> tableDataList = new ArrayList<>(orderablesTableData.values());
        tableGeneratorStep.createTables(tableDataList, jobDefinition);
    }

    private List<String> getOrderables() {
        if (orderableConceptNames == null) {
            JobDefinition ordersJobDefinition = jobDefinitionReader.getJobDefinitionByName(ORDERS_JOB_NAME);
            orderableConceptNames = conceptService.getChildConcepts(ALL_ORDERABLES,
                    ordersJobDefinition.getLocale()).stream()
                    .map(Concept::getName).collect(Collectors.toList());
        }
        return orderableConceptNames;
    }

    private void generateOrderablesTableData(List<String> orderableNames) {
        orderableNames.forEach(orderable -> {
            try {
                String orderSQL = getOrderReaderSQL(orderable);
                TableData tableData = tableDataGenerator.getTableData(orderable, orderSQL);
                orderablesTableData.put(tableData.getName(), tableData);
            } catch (NoSamplesFoundException | InvalidOrderTypeException e) {
                logger.info(e.getMessage());
            } catch (Exception e) {
                logger.error(String
                        .format("An error occurred while retrieving table data for orderable %s", orderable), e);
            }
        });

    }

    private String getOrderReaderSQL(String orderable) throws NoSamplesFoundException, InvalidOrderTypeException {
        int orderTypeId = orderConceptUtil.getOrderTypeId(orderable);
        String orderSQL = convertResourceOutputToString(ordersSQLResource);
        orderSQL = constructSqlWithParameter(orderSQL, "orderTypeId", Integer.toString(orderTypeId));

        JobDefinition ordersJobDefinition = jobDefinitionReader.getJobDefinitionByName(ORDERS_JOB_NAME);
        String readerSql = getReaderSQLByIgnoringColumns(ordersJobDefinition.getColumnsToIgnore(), orderSQL);

        if (Objects.nonNull(getTableData(getProcessedName(orderable))) &&
                !ordersIncrementalUpdateStrategy.isMetaDataChanged(getProcessedName(orderable), ORDERS_JOB_NAME)) {
            String updateOn = ordersJobDefinition.getIncrementalUpdateConfig().getUpdateOn();
            readerSql = ordersIncrementalUpdateStrategy.updateReaderSql(readerSql, ORDERS_JOB_NAME, updateOn);
        }

        return readerSql;
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

    public TableData getTableData(String tableName) {
        return orderablesTableData.get(tableName);
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
        tableDataProcessor.setTableData(getOrdersTableData(orderable));
        return tableDataProcessor;
    }

    private TableData getOrdersTableData(String orderable) throws NoSamplesFoundException, InvalidOrderTypeException {
        return tableDataGenerator.getTableData(orderable, getOrderReaderSQL(orderable));
    }

    private TableRecordWriter ordersDataWriter(String orderable)
            throws InvalidOrderTypeException, NoSamplesFoundException {
        TableRecordWriter writer = recordWriterObjectFactory.getObject();
        writer.setTableData(getOrdersTableData(orderable));
        writer.setJobDefinition(jobDefinitionReader.getJobDefinitionByName(ORDERS_JOB_NAME));
        return writer;
    }
}
