package org.bahmni.analytics.report;

public class JobResult {

    private String dateOfExecution;
    private String status;
    private String zipFileName;

    public JobResult(String dateOfExecution, String status, String zipFileName) {
        this.dateOfExecution = dateOfExecution;
        this.status = status;
        this.zipFileName = zipFileName;
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
