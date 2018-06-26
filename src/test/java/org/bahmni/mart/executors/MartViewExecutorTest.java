package org.bahmni.mart.executors;

import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.view.RegViewDefinition;
import org.bahmni.mart.config.view.ViewDefinition;
import org.bahmni.mart.config.view.ViewExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MartViewExecutorTest {

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private MartJSONReader martJSONReader;

    @Mock
    private ViewExecutor viewExecutor;

    @Mock
    private RegViewDefinition regViewDefinition;

    @Mock
    private JobDefinition jobDefinition;

    private MartViewExecutor martViewExecutor;

    private List<ViewDefinition> viewDefinitions;

    private String registrationSecondPage = "Registration Second Page";


    @Before
    public void setUp() throws Exception {

        martViewExecutor = new MartViewExecutor();

        setValuesForMemberFields(martViewExecutor, "jobDefinitionReader", jobDefinitionReader);
        setValuesForMemberFields(martViewExecutor, "martJSONReader", martJSONReader);
        setValuesForMemberFields(martViewExecutor, "viewExecutor", viewExecutor);
        setValuesForMemberFields(martViewExecutor, "regViewDefinition", regViewDefinition);

        viewDefinitions = new ArrayList<>();
        viewDefinitions.add(new ViewDefinition());
        when(martJSONReader.getViewDefinitions()).thenReturn(viewDefinitions);

        when(jobDefinitionReader.getJobDefinitionByName(registrationSecondPage)).thenReturn(jobDefinition);
    }

    @Test
    public void shouldExecuteViewsWithOutRegView() {

        when(jobDefinition.getName()).thenReturn("");

        martViewExecutor.execute();

        verify(martJSONReader, times(1)).getViewDefinitions();
        verify(jobDefinitionReader, times(1)).getJobDefinitionByName(registrationSecondPage);
        verify(jobDefinition, times(1)).getName();
        verify(viewExecutor).execute(viewDefinitions);
        verify(regViewDefinition, times(0)).getDefinition();
        verify(viewExecutor, times(1)).execute(viewDefinitions);

        assertEquals(1, viewDefinitions.size());

    }

    @Test
    public void shouldExecuteViewsWithRegView() {

        when(jobDefinition.getName()).thenReturn("obs");
        when(regViewDefinition.getDefinition()).thenReturn(mock(ViewDefinition.class));

        martViewExecutor.execute();

        verify(martJSONReader, times(1)).getViewDefinitions();
        verify(jobDefinitionReader, times(1)).getJobDefinitionByName(registrationSecondPage);
        verify(jobDefinition, times(1)).getName();
        verify(viewExecutor).execute(viewDefinitions);
        verify(regViewDefinition, times(1)).getDefinition();
        verify(viewExecutor, times(1)).execute(viewDefinitions);

        assertEquals(2, viewDefinitions.size());


    }
}