package org.bahmni.batch.patient;

import org.apache.commons.io.IOUtils;
import org.bahmni.batch.Person;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughFieldExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

@Component
public class PatientExportStep {
	private DataSource dataSource;

	private StepBuilderFactory stepBuilderFactory;

	private Resource personReportSqlResource;

	private Resource outputFolder;

	@Autowired
	public PatientExportStep(StepBuilderFactory stepBuilderFactory, DataSource dataSource){
		this.dataSource = dataSource;
		this.stepBuilderFactory = stepBuilderFactory;
	}

	@Value("classpath:sql/patientInformation.sql")
	public void setPersonReportSqlResource(Resource personReportSqlResource) {
		this.personReportSqlResource = personReportSqlResource;
	}

	@Value("${outputFolder}/patientInformation.csv")
	public void setOutputFolder(Resource outputFolder) {
		this.outputFolder = outputFolder;
	}

	private String personReportSql() {
		try(InputStream is = personReportSqlResource.getInputStream()) {
			return IOUtils.toString(is);
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot load patientInformation.sql. Unable to continue");
		}
	}

	public Step getStep() {
		return stepBuilderFactory.get("patientInformation")
				.<Person,String>chunk(100)
				.reader(patientInformationReader())
				.writer(flatFileItemWriter())
				.build();
	}

	private JdbcCursorItemReader patientInformationReader(){
		JdbcCursorItemReader reader = new JdbcCursorItemReader();
		reader.setDataSource(dataSource);
		reader.setSql(personReportSql());
		reader.setRowMapper(new ColumnMapRowMapper());
		return reader;
	}

	private FlatFileItemWriter<String> flatFileItemWriter(){
		FlatFileItemWriter<String> writer = new FlatFileItemWriter<String>();
		writer.setResource(outputFolder);
		DelimitedLineAggregator delimitedLineAggregator = new DelimitedLineAggregator();
		delimitedLineAggregator.setDelimiter(",");
		delimitedLineAggregator.setFieldExtractor(new PassThroughFieldExtractor());
		writer.setLineAggregator(delimitedLineAggregator);
		return writer;
	}
}
