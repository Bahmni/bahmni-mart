package org.bahmni.mart.config;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.exception.InvalidOrderTypeException;
import org.bahmni.mart.exception.NoSamplesFoundException;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.bahmni.mart.helper.OrderConceptUtil;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.util.Collections;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BatchUtils.class)
public class OrderStepConfigurerTest {

    @Mock
    private ObsService obsService;

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

    private  OrderStepConfigurer orderStepConfigurer;

    private String orderables = "All Orderables";

    private String orderable = "Lab Samples";

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        orderStepConfigurer = new OrderStepConfigurer();
        setValuesForMemberFields(orderStepConfigurer, "obsService", obsService);
        setValuesForMemberFields(orderStepConfigurer, "tableGeneratorStep", tableGeneratorStep);
        setValuesForMemberFields(orderStepConfigurer, "orderConceptUtil", orderConceptUtil);
        setValuesForMemberFields(orderStepConfigurer, "tableDataGenerator", tableDataGenerator);
        setValueForFinalStaticField(OrderStepConfigurer.class, "logger", logger);
        mockStatic(BatchUtils.class);
    }

    @Test
    public void shouldCreateTablesForAllOrderables() throws InvalidOrderTypeException, NoSamplesFoundException {
        int orderTypeId = 1;
        String sql = "sql";
        when(obsService.getChildConcepts(orderables)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenReturn(orderTypeId);
        when(BatchUtils.convertResourceOutputToString(any())).thenReturn(sql);
        when(BatchUtils.constructSqlWithParameter(sql, "orderTypeId", "1")).thenReturn(sql);
        when(tableDataGenerator.getTableData(orderable, sql)).thenReturn(tableData);

        orderStepConfigurer.createTables();

        verify(tableGeneratorStep, times(1)).createTables(Collections.singletonList(tableData));
        verify(obsService, times(1)).getChildConcepts(orderables);
        verify(concept, times(1)).getName();
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(tableDataGenerator, times(1)).getTableData(orderable, sql);
    }

    @Test
    public void shouldLogExceptionMessageWhenThereAreNoSamplesForAnOrderable()
            throws InvalidOrderTypeException, NoSamplesFoundException {
        String exceptionMessage = "No samples found exception";
        when(obsService.getChildConcepts(orderables)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenThrow(new NoSamplesFoundException(exceptionMessage));

        orderStepConfigurer.createTables();

        verify(tableGeneratorStep, times(1)).createTables(Collections.emptyList());
        verify(obsService, times(1)).getChildConcepts(orderables);
        verify(concept, times(1)).getName();
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(logger, times(1)).info(exceptionMessage);
    }

    @Test
    public void shouldLogExceptionMessageWhenThereIsNoOrderTypeFoundForASample()
            throws InvalidOrderTypeException, NoSamplesFoundException {
        String exceptionMessage = "Invalid order type exception";
        when(obsService.getChildConcepts(orderables)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenThrow(new InvalidOrderTypeException(exceptionMessage));

        orderStepConfigurer.createTables();

        verify(tableGeneratorStep, times(1)).createTables(Collections.emptyList());
        verify(obsService, times(1)).getChildConcepts(orderables);
        verify(concept, times(1)).getName();
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(logger, times(1)).info(exceptionMessage);
    }

    @Test
    public void shouldLogErrorMessageWhenAnyExceptionIsThrownWhileCreatingTables()
            throws InvalidOrderTypeException, NoSamplesFoundException {
        when(obsService.getChildConcepts(orderables)).thenReturn(Collections.singletonList(concept));
        when(concept.getName()).thenReturn(orderable);
        when(orderConceptUtil.getOrderTypeId(orderable)).thenThrow(Exception.class);

        orderStepConfigurer.createTables();

        verify(tableGeneratorStep, times(1)).createTables(Collections.emptyList());
        verify(obsService, times(1)).getChildConcepts(orderables);
        verify(concept, times(1)).getName();
        verify(orderConceptUtil, times(1)).getOrderTypeId(orderable);
        verify(logger, times(1))
                .error(eq("An error occurred while retrieving table data for orderable Lab Samples"),
                        any(Exception.class));
    }
}