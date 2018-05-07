package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.procedure.ProcedureDefinition;
import org.bahmni.mart.config.view.ViewDefinition;

import java.util.List;

public class BahmniMartJSON {

    private List<JobDefinition> jobs;
    private List<ProcedureDefinition> procedures;
    private List<ViewDefinition> views;

    List<JobDefinition> getJobs() {
        return jobs;
    }

    List<ViewDefinition> getViews() {
        return views;
    }

    public List<ProcedureDefinition> getProcedures() {
        return procedures;
    }
}
