package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class JobContext {

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
    private RSPJobStrategy rspJobStrategy;

    @Autowired
    private DispositionJobStrategy dispositionJobStrategy;

    private Map<String, JobStrategy> jobStrategies;

    public Job getJob(JobDefinition jobDefinition) {
        JobStrategy jobStrategy = jobStrategies.get(jobDefinition.getType().toLowerCase());
        if (jobStrategy == null) {
            throw new InvalidJobConfiguration(String.format("Invalid job type '%s' for the job '%s'",
                    jobDefinition.getType(), jobDefinition.getName()));
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
        strategyMap.put(JobType.RSP.toString().toLowerCase(), rspJobStrategy);
        strategyMap.put(JobType.DISPOSITION.toString().toLowerCase(), dispositionJobStrategy);
        return strategyMap;
    }
}
