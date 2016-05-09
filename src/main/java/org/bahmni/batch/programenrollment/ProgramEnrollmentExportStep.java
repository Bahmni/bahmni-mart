package org.bahmni.batch.programenrollment;

import org.apache.commons.io.IOUtils;
import org.bahmni.batch.Person;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughFieldExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ProgramEnrollmentExportStep {

	private StepBuilderFactory stepBuilderFactory;

	private DataSource dataSource;

	@Value("classpath:sql/programEnrollmentReport.sql")
	private Resource programEnrollmentReportSqlResource;

	private Resource outputFolder;

	@Autowired
	public ProgramEnrollmentExportStep(StepBuilderFactory stepBuilderFactory, DataSource dataSource){
		this.dataSource = dataSource;
		this.stepBuilderFactory = stepBuilderFactory;
	}

	@Value("${outputFolder}/programEnrollment.csv")
	public void setOutputFolder(Resource outputFolder) {
		this.outputFolder = outputFolder;
	}

	private String programEnrollmentReportSql() {
		try(InputStream is = programEnrollmentReportSqlResource.getInputStream()) {
			return IOUtils.toString(is);
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot load programEnrollmentReport.sql. Unable to continue");
		}
	}

	public Step getStep() {
		return stepBuilderFactory.get("programEnrollment")
				.<Person,String>chunk(100)
				.reader(programEnrollmentReader())
				.writer(programEnrollmentItemWriter())
				.build();
	}

	private JdbcCursorItemReader programEnrollmentReader(){
		JdbcCursorItemReader reader = new JdbcCursorItemReader();
		reader.setDataSource(dataSource);
		reader.setSql(programEnrollmentReportSql());
		reader.setRowMapper(new ColumnMapRowMapper());
		return reader;
	}

	private ItemWriter<? super String> programEnrollmentItemWriter() {
		FlatFileItemWriter<String> writer = new FlatFileItemWriter<String>();
		writer.setResource(outputFolder);
		DelimitedLineAggregator delimitedLineAggregator = new DelimitedLineAggregator();
		delimitedLineAggregator.setDelimiter(",");
		delimitedLineAggregator.setFieldExtractor(new PassThroughFieldExtractor());
		writer.setLineAggregator(delimitedLineAggregator);
		return writer;

	}

}
