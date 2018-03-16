package org.bahmni.mart.dbconfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {

    @Primary
    @Bean(name = "openmrsDb")
    @ConfigurationProperties(prefix = "spring.ds_openmrs")
    public DataSource openmrsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "openmrsJdbcTemplate")
    public JdbcTemplate openmrsJdbcTemplate(@Qualifier("openmrsDb") DataSource dsOpenmrs) {
        return new JdbcTemplate(dsOpenmrs);
    }

    @Bean(name = "martDb")
    @ConfigurationProperties(prefix = "spring.ds_mart")
    public DataSource martDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "martJdbcTemplate")
    public JdbcTemplate martJdbcTemplate(@Qualifier("martDb") DataSource dsMart) {
        return new JdbcTemplate(dsMart);
    }

}