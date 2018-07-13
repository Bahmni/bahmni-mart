package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.model.CodeConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.CodesProcessor;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.bahmni.mart.table.model.EAV;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionValidator.isValid;

@Component
@Scope(value = "prototype")
public class EAVJobTemplate extends JobTemplate {

    @Autowired
    private FreeMarkerEvaluator<EAV> freeMarkerEvaluator;

    @Autowired
    private EAVJobListener eavJobListener;

    @Autowired
    private CodesProcessor codesProcessor;

    public Job buildJob(JobDefinition jobConfiguration) {
        List<CodeConfig> codeConfigs = jobConfiguration.getCodeConfigs();
        if (isValid(codeConfigs)) {
            codesProcessor.setCodeConfigs(codeConfigs);
            eavJobListener.setCodesProcessor(codesProcessor);
            setPreProcessor(codesProcessor);
        }
        return buildJob(jobConfiguration, eavJobListener, getReaderSql(jobConfiguration));
    }

    private String getReaderSql(JobDefinition jobDefinition) {
        TableData tableData = eavJobListener.getTableDataForMart(jobDefinition.getName());
        String readerSql = freeMarkerEvaluator.evaluate("attribute.ftl",
                new EAV(tableData, jobDefinition.getEavAttributes()));

        return getUpdatedReaderSql(jobDefinition, readerSql);
    }
}
