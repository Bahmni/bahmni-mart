package org.bahmni.mart;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.task.repository.support.TaskRepositoryInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TaskConfiguration {

    @Bean
    public TaskRepositoryInitializer taskRepositoryInitializerInDataMart(
            @Qualifier("scdfDb") DataSource dataSource) throws Exception {

        TaskRepositoryInitializer taskRepositoryInitializer = new TaskRepositoryInitializer();
        taskRepositoryInitializer.setDataSource(dataSource);
        taskRepositoryInitializer.afterPropertiesSet();
        return taskRepositoryInitializer;
    }
}
