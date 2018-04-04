package org.bahmni.mart.config.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ReaderSQLFileLoader {

    private static ApplicationContext applicationContext;

    @Autowired
    ReaderSQLFileLoader(ApplicationContext appContext) {
        applicationContext = appContext;
    }

    static Resource loadResource(String filePath) {
        return applicationContext.getResource(filePath);
    }
}
