package org.bahmni.analytics.dbconfig;

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
    @Bean(name = "mysqlDb")
    @ConfigurationProperties(prefix = "spring.ds_mysql")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("mysqlDb") DataSource dsMySql) {
        return new JdbcTemplate(dsMySql);
    }

    @Bean(name = "postgresqlDb")
    @ConfigurationProperties(prefix = "spring.ds_psql")
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "postgresJdbcTemplate")
    public JdbcTemplate postgresJdbcTemplate(@Qualifier("postgresqlDb") DataSource dsPostgres) {
        return new JdbcTemplate(dsPostgres);
    }

}