package org.bahmni.mart.config;

import org.bahmni.mart.exports.ProgramMetaDataGenerator;
import org.bahmni.mart.table.TableExportStep;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.beans.factory.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ProgramDataStepConfigurerTest {
    @Mock
    private TableGeneratorStep tableGeneratorStep;

    @Mock
    private ProgramMetaDataGenerator programMetaDataGenerator;

    @Mock
    private ObjectFactory<TableExportStep> tablesExportStepObjectFactory;

    @Mock
    private FlowBuilder<FlowJobBuilder> completeDataExport;

    private ProgramDataStepConfigurer programDataStepConfigurer;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        programDataStepConfigurer = new ProgramDataStepConfigurer();
        setValuesForMemberFields(programDataStepConfigurer, "tableGeneratorStep", tableGeneratorStep);
        setValuesForMemberFields(programDataStepConfigurer, "programMetaDataGenerator", programMetaDataGenerator);
        setValuesForMemberFields(programDataStepConfigurer, "tablesExportStepObjectFactory",
                tablesExportStepObjectFactory);
    }

    @Test
    public void shouldCallCreateTables() {
        List<TableData> tableDataList = new ArrayList<>();
        when(programMetaDataGenerator.getTableDataList()).thenReturn(tableDataList);

        programDataStepConfigurer.createTables();

        verify(tableGeneratorStep, times(1)).createTables(tableDataList);
    }

    @Test
    public void shouldRegisterStepsForProgramData() {
        TableExportStep programExportStep = Mockito.mock(TableExportStep.class);
        TableExportStep episodeExportStep = mock(TableExportStep.class);
        List<TableData> tableDataList = new ArrayList<>();
        TableData program = new TableData();
        TableData episode = new TableData();
        tableDataList.add(program);
        tableDataList.add(episode);
        when(tablesExportStepObjectFactory.getObject()).thenReturn(programExportStep).thenReturn(episodeExportStep);
        when(programMetaDataGenerator.getTableDataList()).thenReturn(tableDataList);
        Step programStep = mock(Step.class);
        Step episodeStep = mock(Step.class);
        when(programExportStep.getStep()).thenReturn(programStep);
        when(episodeExportStep.getStep()).thenReturn(episodeStep);

        programDataStepConfigurer.registerSteps(completeDataExport);

        verify(programMetaDataGenerator, times(1)).getTableDataList();
        verify(tablesExportStepObjectFactory, times(2)).getObject();
        verify(programExportStep, times(1)).setTableData(program);
        verify(episodeExportStep, times(1)).setTableData(episode);
        verify(programExportStep, times(1)).getStep();
        verify(episodeExportStep, times(1)).getStep();
        verify(completeDataExport, times(1)).next(programStep);
        verify(completeDataExport, times(1)).next(episodeStep);

    }
}