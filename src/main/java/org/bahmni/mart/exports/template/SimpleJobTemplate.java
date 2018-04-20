package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.CodeConfig;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.table.CodesProcessor;
import org.bahmni.mart.table.listener.TableGeneratorJobListener;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQL;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQLByIgnoringColumns;
import static org.bahmni.mart.config.job.JobDefinitionValidator.isValid;

@Component
public class SimpleJobTemplate extends JobTemplate {

    @Autowired
    private TableGeneratorJobListener tableGeneratorJobListener;

    @Autowired
    private CodesProcessor codesProcessor;

    public Job buildJob(JobDefinition jobConfiguration) {
        List<String> columnsToIgnore = jobConfiguration.getColumnsToIgnore();
        String readerSQL = getReaderSQL(jobConfiguration);
        String readerSQLWithOutIgnoreColumns = getReaderSQLByIgnoringColumns(columnsToIgnore, readerSQL);
        List<CodeConfig> codeConfigs = jobConfiguration.getCodeConfigs();
        if (isValid(codeConfigs)) {
            codesProcessor.setUpCodesData(codeConfigs);
            setPreProcessor(codesProcessor);
        }
        return buildJob(jobConfiguration, tableGeneratorJobListener, readerSQLWithOutIgnoreColumns);
    }
}
