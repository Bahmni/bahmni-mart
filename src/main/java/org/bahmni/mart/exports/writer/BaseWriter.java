package org.bahmni.mart.exports.writer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

public class BaseWriter {
    @Qualifier("martJdbcTemplate")
    @Autowired
    protected JdbcTemplate martJdbcTemplate;

    @Autowired
    protected IncrementalStrategyContext incrementalStrategyContext;

    protected JobDefinition jobDefinition;

    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }
}
