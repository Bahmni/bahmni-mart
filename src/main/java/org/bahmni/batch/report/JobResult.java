package org.bahmni.batch.report;

public class JobResult {

	private String dateOfExecution;
	private String status;
	private String zipFileName;
	private String message;

	public JobResult(String dateOfExecution, String status, String zipFileName, String message) {
		this.dateOfExecution = dateOfExecution;
		this.status = status;
		this.zipFileName = zipFileName;
		this.message = message;
	}

	public String getDateOfExecution() {
		return dateOfExecution;
	}

	public void setDateOfExecution(String dateOfExecution) {
		this.dateOfExecution = dateOfExecution;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}
}
