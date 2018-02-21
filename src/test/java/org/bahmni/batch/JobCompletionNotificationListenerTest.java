package org.bahmni.batch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bahmni.batch.exception.BatchResourceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@PrepareForTest({JobCompletionNotificationListener.class, FileUtils.class, IOUtils.class})
@RunWith(PowerMockRunner.class)
public class JobCompletionNotificationListenerTest {
    private static final String OUTPUT_FILE_NAME_CONTEXT_KEY = "outputFileName";
    private JobCompletionNotificationListener jobCompletionNotificationListener;
    @Mock
    ClassPathResource outputFolder;

    @Mock
    ClassPathResource zipFolder;

    @Rule
    ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(IOUtils.class);
        jobCompletionNotificationListener = new JobCompletionNotificationListener(outputFolder, zipFolder);
    }

    @Test
    public void shouldExecuteBeforeJob() throws Exception {
        JobExecution jobExecution = mock(JobExecution.class);
        File outputFile = mock(File.class);
        when(outputFolder.getFile()).thenReturn(outputFile);
        Date date201711011700 = new Date(1509535850932L);
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date201711011700);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);

        jobCompletionNotificationListener.beforeJob(jobExecution);

        verify(jobExecution, times(1)).getExecutionContext();
        verify(executionContext, times(1)).put(OUTPUT_FILE_NAME_CONTEXT_KEY, "ammanExports201711011700.zip");
    }

    @Test
    public void shouldExecuteBeforeJobAndAnBatchResourceExceptionShouldBeThrown() throws Exception {
        JobExecution jobExecution = mock(JobExecution.class);
        String outputFileName = "outputFileName";
        when(outputFolder.getFilename()).thenReturn(outputFileName);
        when(outputFolder.getFile()).thenThrow(new IOException());

        expectedException.expect(BatchResourceException.class);
        String exceptionMessage = "Cannot create a temporary folder provided as "
                + "'outputFolder' configuration [" + outputFolder.getFilename() + "]";
        expectedException.expectMessage(exceptionMessage);

        jobCompletionNotificationListener.beforeJob(jobExecution);

    }

    @Test
    public void shouldExecuteAfterJob() throws Exception {
        JobExecution jobExecution = mock(JobExecution.class);
        File outputFile = mock(File.class);
        when(outputFolder.getFile()).thenReturn(outputFile);

        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);

        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);

        String zipFileName = "ammanExports201711011700.zip";
        when(executionContext.getString(OUTPUT_FILE_NAME_CONTEXT_KEY)).thenReturn(zipFileName);

        File zipFile = mock(File.class);
        when(zipFolder.getFile()).thenReturn(zipFile);
        PowerMockito.whenNew(File.class).withArguments(zipFile, zipFileName).thenReturn(zipFile);

        FileOutputStream fileOutputStream = mock(FileOutputStream.class);
        PowerMockito.whenNew(FileOutputStream.class).withArguments(zipFile).thenReturn(fileOutputStream);
        ZipOutputStream zipOutputStream = mock(ZipOutputStream.class);
        PowerMockito.whenNew(ZipOutputStream.class).withArguments(fileOutputStream).thenReturn(zipOutputStream);

        File csv1 = mock(File.class);
        File csv2 = mock(File.class);
        List<File> csvDocuments = Arrays.asList(csv1);
        Iterator<File> fileIterator = csvDocuments.iterator();
        when(FileUtils.iterateFiles(eq(outputFile), any(), eq(false))).thenReturn(fileIterator);

        FileInputStream fileInputStream = mock(FileInputStream.class);
        PowerMockito.whenNew(FileInputStream.class).withArguments(csv1).thenReturn(fileInputStream);

        ZipEntry zipEntry = mock(ZipEntry.class);
        when(csv1.getName()).thenReturn("csv1");
        PowerMockito.whenNew(ZipEntry.class).withArguments(csv1.getName()).thenReturn(zipEntry);
        when(fileInputStream.read(any())).thenReturn(10).thenReturn(-1);

        jobCompletionNotificationListener.afterJob(jobExecution);

        verify(jobExecution, times(1)).getStatus();
        verify(jobExecution, times(1)).getExecutionContext();
        verify(executionContext, times(1)).getString(OUTPUT_FILE_NAME_CONTEXT_KEY);
        verify(zipFolder, times(2)).getFile();
        PowerMockito.verifyStatic(times(1));
        FileUtils.iterateFiles(eq(outputFile), any(), eq(false));

        verify(zipOutputStream, times(1)).putNextEntry(zipEntry);
        verify(fileInputStream, times(2)).read(any());
        verify(zipOutputStream, times(1)).write(any(), eq(0), anyInt());
        verify(zipOutputStream, times(1)).closeEntry();
        verify(fileInputStream, times(1)).close();

        PowerMockito.verifyStatic(times(1));
        IOUtils.closeQuietly(zipOutputStream);
        PowerMockito.verifyStatic(times(1));
        IOUtils.closeQuietly(fileOutputStream);
        PowerMockito.verifyStatic(times(1));
        IOUtils.closeQuietly(fileInputStream);
    }

    @Test
    public void shouldThrowAnExceptionWhenAfterJobNotAbleToWriteContentsToOutputFile() throws Exception {
        JobExecution jobExecution = mock(JobExecution.class);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        String zipFileName = "ammanExports201711011700.zip";
        when(executionContext.getString(OUTPUT_FILE_NAME_CONTEXT_KEY)).thenReturn(zipFileName);
        File zipFile = mock(File.class);
        when(zipFolder.getFile()).thenReturn(zipFile);
        PowerMockito.whenNew(File.class).withArguments(zipFile, zipFileName).thenReturn(zipFile);
        FileOutputStream fileOutputStream = mock(FileOutputStream.class);
        PowerMockito.whenNew(FileOutputStream.class).withArguments(zipFile).thenReturn(fileOutputStream);
        ZipOutputStream zipOutputStream = mock(ZipOutputStream.class);
        PowerMockito.whenNew(ZipOutputStream.class).withArguments(fileOutputStream).thenReturn(zipOutputStream);
        File outputFile = mock(File.class);
        when(outputFolder.getFile()).thenThrow(new IOException());
        expectedException.expect(BatchResourceException.class);
        expectedException.expectMessage("Unable to write the output to the [" + zipFile + "]");

        jobCompletionNotificationListener.afterJob(jobExecution);

        verify(jobExecution, times(1)).getStatus();
        verify(jobExecution, times(1)).getExecutionContext();
        verify(executionContext, times(1)).getString(OUTPUT_FILE_NAME_CONTEXT_KEY);
        verify(zipFolder, times(2)).getFile();
        PowerMockito.verifyStatic(times(1));
        IOUtils.closeQuietly(zipOutputStream);
        PowerMockito.verifyStatic(times(1));
        IOUtils.closeQuietly(fileOutputStream);
    }
}