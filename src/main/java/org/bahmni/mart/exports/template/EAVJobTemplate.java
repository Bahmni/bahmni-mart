package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.CodesProcessor;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.bahmni.mart.table.model.EAV;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.bahmni.mart.config.job.JobDefinitionValidator.isValid;

@Component
public class EAVJobTemplate extends JobTemplate {

    @Autowired
    private FreeMarkerEvaluator<EAV> freeMarkerEvaluator;

    @Autowired
    private EAVJobListener eavJobListener;

    @Autowired
    private CodesProcessor codesProcessor;

    public Job buildJob(JobDefinition jobConfiguration) {
        if (isValid(jobConfiguration.getCodeConfigs())) {
            codesProcessor.setUpCodesData(jobConfiguration.getCodeConfigs());
            setPreProcessor(codesProcessor);
        }
        return buildJob(jobConfiguration, eavJobListener, getReaderSql(jobConfiguration));
    }

    private String getReaderSql(JobDefinition jobConfiguration) {
        TableData tableData = eavJobListener.getTableDataForMart(jobConfiguration.getName());
        return freeMarkerEvaluator.evaluate("attribute.ftl",
                new EAV(tableData, jobConfiguration.getEavAttributes()));
    }
}
