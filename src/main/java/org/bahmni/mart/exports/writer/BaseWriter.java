package org.bahmni.mart.exports.writer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.IncrementalUpdateStrategy;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

public abstract class BaseWriter {

    private Set<String> processedIds = new HashSet<>();

    @Qualifier("martJdbcTemplate")
    @Autowired
    protected JdbcTemplate martJdbcTemplate;

    protected JobDefinition jobDefinition;

    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }

    private void deleteVoidedRecords(List<?> items, TableData tableData, IncrementalUpdateStrategy incrementalUpdater) {
        Set<String> voidedIds = getVoidedIds(items);
        voidedIds.removeAll(processedIds);

        incrementalUpdater.deleteVoidedRecords(voidedIds, tableData.getName(),
                jobDefinition.getIncrementalUpdateConfig().getUpdateOn());
        processedIds = new HashSet<>(voidedIds);
    }

    protected abstract Set<String> getVoidedIds(List<?> items);

    protected void deletedVoidedRecords(List<?> items, IncrementalUpdateStrategy incrementalUpdater,
                                        String keyName, TableData tableData) {
        if (isMetadataSame(incrementalUpdater, jobDefinition, keyName)) {
            deleteVoidedRecords(items, tableData, incrementalUpdater);
        }
    }

    private boolean isMetadataSame(IncrementalUpdateStrategy incrementalUpdater, JobDefinition jobDefinition,
                                   String keyName) {
        return !isNull(jobDefinition) && !isNull(jobDefinition.getIncrementalUpdateConfig()) &&
                !incrementalUpdater.isMetaDataChanged(keyName);
    }

    public Set<String> getProcessedIds() {
        return processedIds;
    }
}
