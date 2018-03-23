package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.bahmni.mart.table.model.EAV;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EAVJobTemplate extends JobTemplate {

    @Autowired
    private FreeMarkerEvaluator<EAV> freeMarkerEvaluator;

    @Autowired
    private EAVJobListener eavJobListener;

    public Job buildJob(JobDefinition jobConfiguration) {
        return buildJob(jobConfiguration, eavJobListener, getReaderSql(jobConfiguration));
    }

    private String getReaderSql(JobDefinition jobConfiguration) {
        TableData tableData = eavJobListener.getTableDataForMart(jobConfiguration.getName());
        return freeMarkerEvaluator.evaluate("attribute.ftl",
                new EAV(tableData, jobConfiguration.getEavAttributes()));
    }
}
