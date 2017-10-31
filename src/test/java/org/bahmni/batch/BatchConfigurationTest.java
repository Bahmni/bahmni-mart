package org.bahmni.batch;

import org.apache.commons.io.FileUtils;
import org.bahmni.batch.exception.BatchResourceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(FileUtils.class)
@RunWith(PowerMockRunner.class)
public class BatchConfigurationTest {

    private BatchConfiguration batchConfiguration;

    @Mock
    private ClassPathResource bahmniConfigFolder;

    @Mock
    private Resource zipFolder;

    @Mock
    private ReportGenerator reportGenerator;

    @Rule
    ExpectedException expectedException = ExpectedException.none();


    @Before
    public void setUp() throws Exception {
        mockStatic(FileUtils.class);
        batchConfiguration = new BatchConfiguration();
        setValuesForMemberFields(batchConfiguration, "bahmniConfigFolder", bahmniConfigFolder);
        setValuesForMemberFields(batchConfiguration, "reportGenerator", reportGenerator);
        setValuesForMemberFields(batchConfiguration, "zipFolder", zipFolder);
    }

    @Test
    public void shouldGenerateReportBeforeExitingBatchJob() throws Exception {
        File configFolder = new File("");
        when(bahmniConfigFolder.getFile()).thenReturn(configFolder);
        File reportsFile = Mockito.mock(File.class);
        whenNew(File.class).withArguments(reportsFile, "report.html").thenReturn(reportsFile);
        String generatedReport = "Generated Report";
        when(reportGenerator.generateReport()).thenReturn(generatedReport);

        batchConfiguration.generateReport();

        verify(bahmniConfigFolder, times(1)).getFile();
        verify(reportGenerator, times(1)).generateReport();
    }

    @Test
    public void shouldThrowAnExceptionWhileGeneratingGenerateReport() throws Exception {
        when(bahmniConfigFolder.getFile()).thenThrow(new IOException());
        String zipFileName = "amman-exports-DDMMYYYY.zip";
        when(zipFolder.getFilename()).thenReturn(zipFileName);
        expectedException.expect(BatchResourceException.class);
        expectedException.expectMessage("Unable to write the report file ["+ zipFileName +"]");

        batchConfiguration.generateReport();

        verify(bahmniConfigFolder, times(1)).getFile();
        verify(zipFolder, times(1)).getFilename();
    }

    private void setValuesForMemberFields(Object batchConfiguration, String fieldName, Object valueForMemberField) throws NoSuchFieldException, IllegalAccessException {
        Field f1 = batchConfiguration.getClass().getDeclaredField(fieldName);
        f1.setAccessible(true);
        f1.set(batchConfiguration, valueForMemberField);
    }
}