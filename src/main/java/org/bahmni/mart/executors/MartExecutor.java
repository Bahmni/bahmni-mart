package org.bahmni.mart.executors;

import java.util.List;

public interface MartExecutor {
    void execute();

    List<String> getFailedJobs();
}

