package org.bahmni.mart.config.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class SQLFileLoader {

    private static ApplicationContext applicationContext;

    @Autowired
    SQLFileLoader(ApplicationContext appContext) {
        applicationContext = appContext;
    }

    public static Resource loadResource(String filePath) {
        return applicationContext.getResource(filePath);
    }
}
