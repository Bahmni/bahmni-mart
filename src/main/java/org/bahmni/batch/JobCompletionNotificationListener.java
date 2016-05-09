package org.bahmni.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

	@Override
	public void beforeJob(JobExecution jobExecution) {
		//TODO: Do a setup.. create a folder with a timestamp and make it ready for CSVs
		//Create a folder in /var/www/endtbexports with timestamp
		//
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		//TODO: zip all the files in the folder created
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("!!! JOB FINISHED! Time to verify the results");
			//ZIP all the output files
		}
	}
}
