package org.bahmni.mart.config;

import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.beans.factory.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormStepConfigurerTest {

    private FormStepConfigurer formStepConfigurer;

    @Mock
    private TableGeneratorStep tableGeneratorStep;

    @Mock
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Mock
    private FormListProcessor formListProcessor;

    @Mock
    private ObjectFactory<ObservationExportStep> observationExportStepFactory;

    @Mock
    private FlowBuilder<FlowJobBuilder> completeDataExport;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        formStepConfigurer = new FormStepConfigurer();
        setValuesForMemberFields(formStepConfigurer, "tableGeneratorStep", tableGeneratorStep);
        setValuesForMemberFields(formStepConfigurer, "formTableMetadataGenerator", formTableMetadataGenerator);
        setValuesForMemberFields(formStepConfigurer, "formListProcessor", formListProcessor);
        setValuesForMemberFields(formStepConfigurer, "observationExportStepFactory", observationExportStepFactory);
    }

    @Test
    public void shouldCallCreateTables() {
        List<TableData> tableDataList = new ArrayList<>();
        when(formTableMetadataGenerator.getTableDataList()).thenReturn(tableDataList);

        formStepConfigurer.createTables();

        verify(tableGeneratorStep, times(1)).createTables(tableDataList);
    }

    @Test
    public void shouldRegisterObservationStepsForTwoForms() {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();

        BahmniForm medicalHistoryForm = new BahmniForm();
        BahmniForm fstg = new BahmniForm();
        bahmniForms.add(medicalHistoryForm);
        bahmniForms.add(fstg);

        Step medicalHistoryStep = mock(Step.class);
        Step fstgStep = mock(Step.class);

        when(formListProcessor.retrieveAllForms()).thenReturn(bahmniForms);
        ObservationExportStep medicalHistoryObservationExportStep = mock(ObservationExportStep.class);
        ObservationExportStep fstgObservationExportStep = mock(ObservationExportStep.class);
        when(observationExportStepFactory.getObject()).thenReturn(medicalHistoryObservationExportStep)
                .thenReturn(fstgObservationExportStep);
        when(medicalHistoryObservationExportStep.getStep()).thenReturn(medicalHistoryStep);
        when(fstgObservationExportStep.getStep()).thenReturn(fstgStep);

        formStepConfigurer.registerSteps(completeDataExport);

        verify(formListProcessor, times(1)).retrieveAllForms();
        verify(observationExportStepFactory,times(2)).getObject();
        verify(medicalHistoryObservationExportStep, times(1)).setForm(medicalHistoryForm);
        verify(fstgObservationExportStep, times(1)).setForm(fstg);
        verify(medicalHistoryObservationExportStep, times(1)).getStep();
        verify(fstgObservationExportStep, times(1)).getStep();
        verify(completeDataExport,times(1)).next(medicalHistoryStep);
        verify(completeDataExport,times(1)).next(fstgStep);
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(medicalHistoryForm);
        verify(formTableMetadataGenerator, times(1)).addMetadataForForm(fstg);
    }
    
}