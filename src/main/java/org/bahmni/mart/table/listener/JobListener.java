package org.bahmni.mart.table.listener;

import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.core.JobExecutionListener;

public interface JobListener extends JobExecutionListener {
    TableData getTableDataForMart(String jobName);
}
