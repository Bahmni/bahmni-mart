package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class JobContext {
    private static final Logger log = LoggerFactory.getLogger(JobContext.class);

    @Autowired
    private CustomSqlJobStrategy customSqlJobStrategy;

    @Autowired
    private ObsJobStrategy obsJobStrategy;

    @Autowired
    private EAVJobStrategy eavJobStrategy;

    @Autowired
    private BacteriologyJobStrategy bacteriologyJobStrategy;

    @Autowired
    private MetaDataJobStrategy metaDataJobStrategy;

    @Autowired
    private OrderJobStrategy orderJobStrategy;

    @Autowired
    private DiagnosesJobStrategy diagnosesJobStrategy;

    @Autowired
    private CSVUploadJobStrategy csvUploadJobStrategy;

    @Autowired
    private REGJobStrategy regJobStrategy;

    @Autowired
    private DispositionJobStrategy dispositionJobStrategy;

    private Map<String, JobStrategy> jobStrategies;

    public Job getJob(JobDefinition jobDefinition) {
        String jobDefinitionType = jobDefinition.getType();

        JobStrategy jobStrategy = jobStrategies.get(jobDefinitionType.toLowerCase());
        if (jobStrategy == null) {
            log.warn(String.format("'%s' type is invalid for the job '%s'",
                    jobDefinitionType, jobDefinition.getName()));
            return null;
        }
        return jobStrategy.getJob(jobDefinition);
    }

    @PostConstruct
    private void instantiateJobStrategies() {
        jobStrategies = createJobStrategies();
    }

    private Map<String, JobStrategy> createJobStrategies() {
        Map<String, JobStrategy> strategyMap = new HashMap<>();
        strategyMap.put(JobType.CUSTOMSQL.toString().toLowerCase(), customSqlJobStrategy);
        strategyMap.put(JobType.OBS.toString().toLowerCase(), obsJobStrategy);
        strategyMap.put(JobType.EAV.toString().toLowerCase(), eavJobStrategy);
        strategyMap.put(JobType.BACTERIOLOGY.toString().toLowerCase(), bacteriologyJobStrategy);
        strategyMap.put(JobType.METADATA.toString().toLowerCase(), metaDataJobStrategy);
        strategyMap.put(JobType.ORDERS.toString().toLowerCase(), orderJobStrategy);
        strategyMap.put(JobType.DIAGNOSES.toString().toLowerCase(), diagnosesJobStrategy);
        strategyMap.put(JobType.CSVUPLOAD.toString().toLowerCase(), csvUploadJobStrategy);
        strategyMap.put(JobType.REG.toString().toLowerCase(), regJobStrategy);
        strategyMap.put(JobType.DISPOSITION.toString().toLowerCase(), dispositionJobStrategy);
        return strategyMap;
    }
}
