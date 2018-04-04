package org.bahmni.mart.config.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReaderSQLFileLoaderTest {

    @Mock
    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {
        setValuesForMemberFields(new ReaderSQLFileLoader(applicationContext), "applicationContext", applicationContext);
    }

    @Test
    public void shouldReturnResourceForGivenFilePath() throws Exception {
        Resource expectedResource = Mockito.mock(Resource.class);
        when(applicationContext.getResource(anyString())).thenReturn(expectedResource);

        Resource actualResource = ReaderSQLFileLoader.loadResource("some path");

        assertEquals(expectedResource, actualResource);
    }

}