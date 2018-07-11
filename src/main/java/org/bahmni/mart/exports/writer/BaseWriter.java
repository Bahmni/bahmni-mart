package org.bahmni.mart.exports.writer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.helper.incrementalupdate.AbstractIncrementalUpdater;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

public abstract class BaseWriter {

    @Qualifier("martJdbcTemplate")
    @Autowired
    protected JdbcTemplate martJdbcTemplate;

    protected JobDefinition jobDefinition;

    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }

    private void deleteVoidedRecords(List<?> items, TableData tableData,
                                     AbstractIncrementalUpdater incrementalUpdater) {
        incrementalUpdater.deleteVoidedRecords(getVoidedIds(items), tableData.getName(),
                jobDefinition.getIncrementalUpdateConfig().getUpdateOn());
    }

    protected abstract Set<String> getVoidedIds(List<?> items);

    protected void deletedVoidedRecords(List<?> items, AbstractIncrementalUpdater incrementalUpdater,
                                        String keyName, TableData tableData) {
        if (isMetadataSame(incrementalUpdater, jobDefinition, keyName)) {
            deleteVoidedRecords(items, tableData, incrementalUpdater);
        }
    }

    private boolean isMetadataSame(AbstractIncrementalUpdater incrementalUpdater, JobDefinition jobDefinition,
                                   String keyName) {
        return !isNull(jobDefinition) && !isNull(jobDefinition.getIncrementalUpdateConfig()) &&
                !incrementalUpdater.isMetaDataChanged(keyName);
    }
}
