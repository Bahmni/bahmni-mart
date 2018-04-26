package org.bahmni.mart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.task.configuration.TaskConfigurer;
import org.springframework.cloud.task.repository.TaskExplorer;
import org.springframework.cloud.task.repository.TaskRepository;
import org.springframework.cloud.task.repository.support.SimpleTaskExplorer;
import org.springframework.cloud.task.repository.support.SimpleTaskRepository;
import org.springframework.cloud.task.repository.support.TaskExecutionDaoFactoryBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Component
public class DataloadTaskConfigurer implements TaskConfigurer {
    private DataSource dataSource;

    private TaskRepository taskRepository;

    private TaskExplorer taskExplorer;

    private PlatformTransactionManager transactionManager;

    @Autowired
    public DataloadTaskConfigurer(@Qualifier("scdfDb") DataSource dataSource) {

        this.dataSource = dataSource;
        TaskExecutionDaoFactoryBean taskExecutionDaoFactoryBean = new TaskExecutionDaoFactoryBean(this.dataSource);
        this.taskRepository = new SimpleTaskRepository(taskExecutionDaoFactoryBean);
        this.taskExplorer = new SimpleTaskExplorer(taskExecutionDaoFactoryBean);
    }

    @Override
    public TaskRepository getTaskRepository() {
        return this.taskRepository;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        if (this.transactionManager == null) {
            this.transactionManager = new DataSourceTransactionManager(this.dataSource);
        }
        return this.transactionManager;
    }

    @Override
    public TaskExplorer getTaskExplorer() {
        return this.taskExplorer;
    }

    public DataSource getTaskDataSource() {
        return this.dataSource;
    }
}


