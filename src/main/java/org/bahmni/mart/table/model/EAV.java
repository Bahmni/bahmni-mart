package org.bahmni.mart.table.model;

import org.bahmni.mart.config.job.EAVJobData;
import org.bahmni.mart.table.domain.TableData;

public class EAV {

    private  TableData tableData;
    private  EAVJobData eavJobData;

    public EAV(TableData tableData, EAVJobData eavJobData) {
        this.tableData = tableData;
        this.eavJobData = eavJobData;
    }

    public TableData getTableData() {
        return tableData;
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    public EAVJobData getEavJobData() {
        return eavJobData;
    }

    public void setEavJobData(EAVJobData eavJobData) {
        this.eavJobData = eavJobData;
    }
}
