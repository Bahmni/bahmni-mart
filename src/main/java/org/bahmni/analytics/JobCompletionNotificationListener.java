package org.bahmni.analytics;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bahmni.analytics.exception.BatchResourceException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    public static final String OUTPUT_FILE_NAME_CONTEXT_KEY = "outputFileName";
    private Resource outputFolder;
    private Resource zipFolder;

    @Autowired
    public JobCompletionNotificationListener(@Value("${outputFolder}") Resource outputFolder,
                                             @Value("${zipFolder}") Resource zipFolder) {
        this.outputFolder = outputFolder;
        this.zipFolder = zipFolder;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {

    }

    private void addToZipFile(File file, ZipOutputStream zos) throws IOException {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            zos.closeEntry();
            fis.close();
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

}
