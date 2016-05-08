package org.bahmni.batch;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@PropertySource("classpath:/batch-mysql.properties")
public class BatchDataSourceConfiguration {
	@Autowired
	private Environment environment;

	@Autowired
	private ResourceLoader resourceLoader;

	@PostConstruct
	protected void initialize() {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(resourceLoader.getResource(environment.getProperty("batch.schema.script")));
		populator.setContinueOnError(true);
		DatabasePopulatorUtils.execute(populator , dataSource());
	}

	@Bean(destroyMethod="close")
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(environment.getProperty("batch.jdbc.driver"));
		dataSource.setUrl(environment.getProperty("batch.jdbc.url"));
		dataSource.setUsername(environment.getProperty("batch.jdbc.user"));
		dataSource.setPassword(environment.getProperty("batch.jdbc.password"));
		return dataSource;
	}


}
