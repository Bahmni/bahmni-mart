package org.bahmni.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private Resource outputFolder;

    @Autowired
    public JobCompletionNotificationListener(@Value("${outputFolder}") Resource outputFolder) {
        this.outputFolder = outputFolder;

    }

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    private String zipFile;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        File folder = null;
        try {
            folder = new File(outputFolder.getURL().getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File[] files = folder.listFiles();

        if (files == null)
            return;

        for (File f : files) {
            f.delete();
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {

            File folder = null;
            try {
                folder = new File(outputFolder.getURL().getFile());
            String zipFileName = new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";
            ZipOutputStream zos = null;
                zos = new ZipOutputStream(new FileOutputStream(outputFolder.getURL().getFile() + "/" + zipFileName));
            createZipForCsvs(folder, zipFileName, zos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createZipForCsvs(File folder, String zipFileName, ZipOutputStream zos) {
        try {

            for (File file : folder.listFiles()) {
                if (!file.getName().equals(zipFileName)) {

                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    byte[] bytes = new byte[1024];
                    int length;


                    FileInputStream fis = new FileInputStream(file);
                    zos.putNextEntry(zipEntry);

                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }

                    zos.closeEntry();
                }
            }

            zos.close();
            log.info("!!! JOB FINISHED! Time to verify the results");
        } catch (FileNotFoundException e) {
            log.error(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
