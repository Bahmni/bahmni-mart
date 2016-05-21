package org.bahmni.batch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Resource outputFolder;

	private Resource zipFolder;

    @Autowired
    public JobCompletionNotificationListener(@Value("${outputFolder}") Resource outputFolder,@Value("${zipFolder}") Resource zipFolder) {
        this.outputFolder = outputFolder;
	    this.zipFolder = zipFolder;

    }

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
	    try {
		    FileUtils.deleteQuietly(outputFolder.getFile());
		    FileUtils.forceMkdir(outputFolder.getFile());
	    }
	    catch (IOException e) {
		    throw new RuntimeException("Cannot create a temporary folder provided as "
				    + "'outputFolder' configuration ["+ outputFolder.getFilename()+"]",e);
	    }

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {

	        FileOutputStream fos = null;
	        ZipOutputStream zos = null;
	        String zipFileName = new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";
	        File zipFile = null;

	        try {
		        FileUtils.forceMkdir(zipFolder.getFile());
		        zipFile = new File(zipFolder.getFile(),zipFileName);
		        fos = new FileOutputStream(zipFile);
		        zos = new ZipOutputStream(fos);
		        Iterator<File> iterator =  FileUtils.iterateFiles(outputFolder.getFile(),new String[]{ "png","csv" },false);
		        while(iterator.hasNext()){
			        File file = iterator.next();
			        addToZipFile(file,zos);
		        }

		        jobExecution.getExecutionContext().put("OutputFile",zipFile.getAbsolutePath());

	        } catch (IOException e) {
		        e.printStackTrace();
	        }finally {
		        IOUtils.closeQuietly(zos);
		        IOUtils.closeQuietly(fos);
	        }

        }
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
		} finally{
			IOUtils.closeQuietly(fis);
		}
	}

}
