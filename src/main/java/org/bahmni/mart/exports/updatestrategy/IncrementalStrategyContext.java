package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.job.JobType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.upperCase;

@Component
public class IncrementalStrategyContext {

    @Autowired
    private CustomSqlIncrementalUpdateStrategy customSqlIncrementalUpdateStrategy;

    @Autowired
    private ObsIncrementalUpdateStrategy obsIncrementalUpdateStrategy;

    @Autowired
    private EavIncrementalUpdateStrategy eavIncrementalUpdateStrategy;

    @Autowired
    private SimpleIncrementalUpdateStrategy simpleIncrementalUpdateStrategy;


    private Map<String, IncrementalUpdateStrategy> incrementalStrategies;

    @PostConstruct
    private void createIncrementalStrategyMap() {
        incrementalStrategies = new HashMap<>();
        incrementalStrategies.put(JobType.CUSTOMSQL.toString(), customSqlIncrementalUpdateStrategy);
        incrementalStrategies.put(JobType.OBS.toString(), obsIncrementalUpdateStrategy);
        incrementalStrategies.put(JobType.EAV.toString(), eavIncrementalUpdateStrategy);
    }

    public IncrementalUpdateStrategy getStrategy(String jobType) {
        return incrementalStrategies.getOrDefault(upperCase(jobType), simpleIncrementalUpdateStrategy);
    }

}

