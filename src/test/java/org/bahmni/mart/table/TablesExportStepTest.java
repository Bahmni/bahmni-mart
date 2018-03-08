package org.bahmni.mart.table;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(BatchUtils.class)
@RunWith(PowerMockRunner.class)
public class TablesExportStepTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private FreeMarkerEvaluator<TableData> tableRecordHolderFreeMarkerEvaluator;

    @Mock
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Mock
    private ResourceLoader resourceLoader;

    private TableExportStep tablesExportStep = new TableExportStep();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(BatchUtils.class);
        setValuesForMemberFields(tablesExportStep, "dataSource", dataSource);
        setValuesForMemberFields(tablesExportStep, "stepBuilderFactory", stepBuilderFactory);
        setValuesForMemberFields(tablesExportStep, "tableRecordHolderFreeMarkerEvaluator",
                tableRecordHolderFreeMarkerEvaluator);
        setValuesForMemberFields(tablesExportStep, "recordWriterObjectFactory", recordWriterObjectFactory);
        setValuesForMemberFields(tablesExportStep, "resourceLoader", resourceLoader);
        BatchUtils.stepNumber = 0;
    }

    @Test
    public void shouldSetTheTable() throws Exception {
        TableData table = mock(TableData.class);
        String lengthyTableName = "moreThanHundredCharacterInTheFormNamemoreThanHundred" +
                "CharacterInTheFormNamemoreThanHundredCharacterInTheFormName";
        when(table.getName()).thenReturn("table").thenReturn(lengthyTableName);

        tablesExportStep.setTableData(table);

        String stepName = tablesExportStep.getStepName();
        assertEquals("Step-1 table", stepName);
        stepName = tablesExportStep.getStepName();
        assertEquals("Step-2 moreThanHundredCharacterInTheFormNamemoreThanHundred" +
                "CharacterInTheFormNamemoreThanHundredChar", stepName);

    }

    @Test
    public void shouldGetTheBatchStepForTableExport() throws Exception {
        TableData table = mock(TableData.class);
        StepBuilder stepBuilder = mock(StepBuilder.class);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
        TaskletStep expectedBaseExportStep = mock(TaskletStep.class);
        tablesExportStep.setTableData(table);
        String tableName = "table";
        when(table.getName()).thenReturn(tableName);
        when(stepBuilderFactory.get("Step-1 " + tableName)).thenReturn(stepBuilder);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(recordWriterObjectFactory.getObject()).thenReturn(new TableRecordWriter());
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.build()).thenReturn(expectedBaseExportStep);

        Step tablesExportStepStep = tablesExportStep.getStep();

        Assert.assertNotNull(tablesExportStepStep);
        Assert.assertEquals(expectedBaseExportStep, tablesExportStepStep);
    }

    private void setValuesForMemberFields(Object observationExportStep, String fieldName, Object valueForMemberField)
            throws NoSuchFieldException, IllegalAccessException {
        Field f1 = observationExportStep.getClass().getDeclaredField(fieldName);
        f1.setAccessible(true);
        f1.set(observationExportStep, valueForMemberField);
    }
}