package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.exception.InvalidOrderTypeException;
import org.bahmni.mart.exception.NoSamplesFoundException;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.bahmni.mart.helper.OrderConceptUtil;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.exports.writer.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BatchUtils.class)
public class OrderStepConfigurerTest {

    @Mock
    private ConceptService conceptService;

    @Mock
    private TableGeneratorStep tableGeneratorStep;

    @Mock
    private OrderConceptUtil orderConceptUtil;

    @Mock
    private TableDataGenerator tableDataGenerator;

    @Mock
    private Concept concept;

    @Mock
    private TableData tableData;

    @Mock
    private Logger logger;

    @Mock
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    private OrderStepConfigurer orderStepConfigurer;

    private String orderables = "All Orderables";

    private String orderable = "Lab Samples";
    private String locale;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        orderStepConfigurer = new OrderStepConfigurer();
        locale = "locale";
        setValuesForMemberFields(orderStepConfigurer, "conceptService", conceptService);
        setValuesForMemberFields(orderStepConfigurer, "tableGeneratorStep", tableGeneratorStep);
        setValuesForMemberFields(orderStepConfigurer, "orderConceptUtil", orderConceptUtil);
        setValuesForMemberFields(orderStepConfigurer, "tableDataGenerator", tableDataGenerator);
        setValuesForMemberFields(orderStepConfigurer, "stepBuilderFactory", stepBuilderFactory);
        setValuesForMemberFields(orderStepConfigurer, "jobDefinitionReader", jobDefinitionReader);
        setValuesForMemberFields(orderStepConfigurer, "recordWriterObjectFactory",
                recordWriterObjectFactory);
        setValueForFinalStaticField(OrderStepConfigurer.class, "logger", logger);

        mockStatic(BatchUtils.class);
        when(jobDefinition.getLocale()).thenReturn(locale);
    }

    @Test
    public void shouldGenerateOrderablesTableData() throws InvalidOrderTypeException, NoSamplesFoundException {
        int orderTypeId = 1;
        String sql = "sql";
        when(conceptService.getChildConcepts(orderables, locale)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenReturn(orderTypeId);
        when(BatchUtils.convertResourceOutputToString(any())).thenReturn(sql);
        when(BatchUtils.constructSqlWithParameter(sql, "orderTypeId", "1")).thenReturn(sql);
        when(tableDataGenerator.getTableData(orderable, sql)).thenReturn(tableData);
        when(jobDefinitionReader.getJobDefinitionByName("Orders Data")).thenReturn(jobDefinition);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Collections.emptyList());

        orderStepConfigurer.generateTableData(jobDefinition);

        verify(conceptService, times(1)).getChildConcepts(orderables, locale);
        verify(concept, times(1)).getName();
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(tableDataGenerator, times(1)).getTableData(orderable, sql);
    }

    @Test
    public void shouldNotCallDBForAllOrderablesFromSecondCallOnwards() throws NoSuchFieldException,
            IllegalAccessException, InvalidOrderTypeException, NoSamplesFoundException {
        int orderTypeId = 1;
        String sql = "sql";
        when(concept.getName()).thenReturn(orderable);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenReturn(orderTypeId);
        when(BatchUtils.convertResourceOutputToString(any())).thenReturn(sql);
        when(BatchUtils.constructSqlWithParameter(sql, "orderTypeId", "1")).thenReturn(sql);
        when(tableDataGenerator.getTableData(orderable, sql)).thenReturn(tableData);
        when(jobDefinitionReader.getJobDefinitionByName("Orders Data")).thenReturn(jobDefinition);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Collections.emptyList());

        setValuesForMemberFields(orderStepConfigurer, "orderableConceptNames", Arrays.asList("Lab Samples"));

        orderStepConfigurer.generateTableData(jobDefinition);

        verify(conceptService, never()).getChildConcepts(orderables, locale);
        verify(jobDefinitionReader).getJobDefinitionByName("Orders Data");
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(tableDataGenerator, times(1)).getTableData(orderable, sql);
    }

    @Test
    public void shouldCreateAllOrderableTables() throws NoSuchFieldException, IllegalAccessException {
        List<TableData> orderablesTableData = Collections.singletonList(mock(TableData.class));
        setValuesForMemberFields(orderStepConfigurer, "orderablesTableData", orderablesTableData);

        orderStepConfigurer.createTables(jobDefinition);

        verify(tableGeneratorStep).createTables(orderablesTableData, jobDefinition);
    }

    @Test
    public void shouldLogExceptionMessageWhenThereAreNoSamplesForAnOrderable()
            throws InvalidOrderTypeException, NoSamplesFoundException {
        String exceptionMessage = "No samples found exception";

        when(jobDefinitionReader.getJobDefinitionByName("Orders Data")).thenReturn(jobDefinition);
        when(conceptService.getChildConcepts(orderables, locale)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenThrow(new NoSamplesFoundException(exceptionMessage));

        orderStepConfigurer.generateTableData(jobDefinition);

        verify(conceptService, times(1)).getChildConcepts(orderables, locale);
        verify(jobDefinitionReader).getJobDefinitionByName("Orders Data");
        verify(jobDefinition).getLocale();
        verify(concept, times(1)).getName();
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(logger, times(1)).info(exceptionMessage);
    }

    @Test
    public void shouldLogExceptionMessageWhenThereIsNoOrderTypeFoundForASample()
            throws InvalidOrderTypeException, NoSamplesFoundException {
        String exceptionMessage = "Invalid order type exception";
        when(jobDefinitionReader.getJobDefinitionByName("Orders Data")).thenReturn(jobDefinition);
        when(conceptService.getChildConcepts(orderables, locale)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenThrow(new InvalidOrderTypeException(exceptionMessage));

        orderStepConfigurer.generateTableData(jobDefinition);

        verify(conceptService, times(1)).getChildConcepts(orderables, locale);
        verify(jobDefinitionReader).getJobDefinitionByName("Orders Data");
        verify(jobDefinition).getLocale();

        verify(concept, times(1)).getName();
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(logger, times(1)).info(exceptionMessage);
    }

    @Test
    public void shouldLogErrorMessageWhenAnyExceptionIsThrownWhileCreatingTables()
            throws InvalidOrderTypeException, NoSamplesFoundException {
        when(jobDefinitionReader.getJobDefinitionByName("Orders Data")).thenReturn(jobDefinition);
        when(conceptService.getChildConcepts(orderables, locale)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenThrow(Exception.class);

        orderStepConfigurer.generateTableData(jobDefinition);

        verify(conceptService, times(1)).getChildConcepts(orderables, locale);
        verify(jobDefinitionReader).getJobDefinitionByName("Orders Data");
        verify(jobDefinition).getLocale();
        verify(concept, times(1)).getName();
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(logger, times(1))
                .error(eq("An error occurred while retrieving table data for orderable Lab Samples"),
                        any(Exception.class));
    }

    @Test
    public void shouldRegisterStepsforAllOrderables() throws InvalidOrderTypeException, NoSamplesFoundException {
        int orderTypeId = 1;
        String sql = "sql";

        StepBuilder stepBuilder = mock(StepBuilder.class);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
        TaskletStep expectedBaseExportStep = mock(TaskletStep.class);
        FlowBuilder completeDataExport = mock(FlowBuilder.class);

        when(conceptService.getChildConcepts(orderables, locale)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);

        when(stepBuilderFactory.get(orderable)).thenReturn(stepBuilder);
        when(jobDefinition.getChunkSizeToRead()).thenReturn(100);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenReturn(orderTypeId);
        when(BatchUtils.convertResourceOutputToString(any())).thenReturn(sql);
        when(BatchUtils.constructSqlWithParameter(sql, "orderTypeId", "1")).thenReturn(sql);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);

        when(tableDataGenerator.getTableData(orderable, sql)).thenReturn(tableData);
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilder);
        when(recordWriterObjectFactory.getObject()).thenReturn(new TableRecordWriter());
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.build()).thenReturn(expectedBaseExportStep);
        when(jobDefinitionReader.getJobDefinitionByName("Orders Data")).thenReturn(jobDefinition);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Collections.emptyList());

        orderStepConfigurer.registerSteps(completeDataExport, jobDefinition);

        verify(conceptService, times(1)).getChildConcepts(orderables, locale);
        verify(concept, times(1)).getName();
        verify(stepBuilderFactory, times(1)).get(orderable);
        verify(jobDefinition, times(1)).getChunkSizeToRead();
        verify(stepBuilder, times(1)).chunk(100);
        verify(orderConceptUtil, times(3)).getOrderTypeId(orderable);
        verifyStatic(times(3));
        BatchUtils.convertResourceOutputToString(any());
        verifyStatic(times(3));
        BatchUtils.constructSqlWithParameter(sql, "orderTypeId", "1");
        verify(simpleStepBuilder, times(1)).reader(any());
        verify(tableDataGenerator, times(2)).getTableData(orderable, sql);
        verify(simpleStepBuilder, times(1)).processor(any());
        verify(recordWriterObjectFactory, times(1)).getObject();
        verify(simpleStepBuilder, times(1)).writer(any());
        verify(simpleStepBuilder, times(1)).build();
        verify(completeDataExport, times(1)).next(any(Step.class));
    }

    @Test
    public void shouldLogExceptionForInvalidOrderType() throws InvalidOrderTypeException, NoSamplesFoundException {
        String exceptionMessage = "Invalid order type exception";

        StepBuilder stepBuilder = mock(StepBuilder.class);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
        FlowBuilder completeDataExport = mock(FlowBuilder.class);

        when(jobDefinitionReader.getJobDefinitionByName("Orders Data")).thenReturn(jobDefinition);
        when(conceptService.getChildConcepts(orderables, locale)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);

        when(stepBuilderFactory.get(orderable)).thenReturn(stepBuilder);
        when(jobDefinition.getChunkSizeToRead()).thenReturn(100);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenThrow(new InvalidOrderTypeException(exceptionMessage));

        orderStepConfigurer.registerSteps(completeDataExport, jobDefinition);

        verify(conceptService, times(1)).getChildConcepts(orderables, locale);
        verify(jobDefinitionReader).getJobDefinitionByName("Orders Data");
        verify(jobDefinition).getLocale();
        verify(concept, times(1)).getName();
        verify(stepBuilderFactory, times(1)).get(orderable);
        verify(jobDefinition, times(1)).getChunkSizeToRead();
        verify(stepBuilder, times(1)).chunk(100);
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(logger, times(1)).info(exceptionMessage);
        verify(completeDataExport, times(0)).next(any(Step.class));
    }

    @Test
    public void shouldLogExceptionForNoSampleFoundException()
            throws InvalidOrderTypeException, NoSamplesFoundException {
        String exceptionMessage = "Invalid order type exception";

        StepBuilder stepBuilder = mock(StepBuilder.class);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
        FlowBuilder completeDataExport = mock(FlowBuilder.class);

        when(jobDefinitionReader.getJobDefinitionByName("Orders Data")).thenReturn(jobDefinition);
        when(conceptService.getChildConcepts(orderables, locale)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);

        when(stepBuilderFactory.get(orderable)).thenReturn(stepBuilder);
        when(jobDefinition.getChunkSizeToRead()).thenReturn(100);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenThrow(new NoSamplesFoundException(exceptionMessage));

        orderStepConfigurer.registerSteps(completeDataExport, jobDefinition);

        verify(conceptService, times(1)).getChildConcepts(orderables, locale);
        verify(jobDefinitionReader).getJobDefinitionByName("Orders Data");
        verify(jobDefinition).getLocale();
        verify(concept, times(1)).getName();
        verify(stepBuilderFactory, times(1)).get(orderable);
        verify(jobDefinition, times(1)).getChunkSizeToRead();
        verify(stepBuilder, times(1)).chunk(100);
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(logger, times(1)).info(exceptionMessage);
        verify(completeDataExport, times(0)).next(any(Step.class));
        verifyStatic(times(1));
        Optional.empty();
    }
}