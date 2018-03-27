package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.view.ViewDefinition;

import java.util.List;

public class BahmniMartJSON {

    private List<JobDefinition> jobs;
    private List<ViewDefinition> views;

    public List<JobDefinition> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobDefinition> jobs) {
        this.jobs = jobs;
    }

    public List<ViewDefinition> getViews() {
        return views;
    }

    public void setViews(List<ViewDefinition> views) {
        this.views = views;
    }
}
