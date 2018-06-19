package org.bahmni.mart.executors;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.view.RspViewDefinition;
import org.bahmni.mart.config.view.ViewDefinition;
import org.bahmni.mart.config.view.ViewExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MartViewExecutor implements MartExecutor {

    private static final String REGISTRATION_SECOND_PAGE = "Registration Second Page";

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Autowired
    private MartJSONReader martJSONReader;

    @Autowired
    private ViewExecutor viewExecutor;

    @Autowired
    private RspViewDefinition rspViewDefinition;

    @Override
    public void execute() {

        List<ViewDefinition> viewDefinitions = martJSONReader.getViewDefinitions();

        if (!StringUtils.isEmpty(jobDefinitionReader.getJobDefinitionByName(REGISTRATION_SECOND_PAGE).getName())) {
            viewDefinitions.add(rspViewDefinition.getDefinition());
        }
        viewExecutor.execute(viewDefinitions);
    }
}
