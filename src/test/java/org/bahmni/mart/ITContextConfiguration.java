package org.bahmni.mart;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class ITContextConfiguration {
    @Bean(name = "customITContext")
    public PlatformTransactionManager customITContext(@Qualifier("openmrsDb") DataSource openmrsDb) {
        return new DataSourceTransactionManager(openmrsDb);
    }
}
