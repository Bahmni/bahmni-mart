package org.bahmni.batch;

import freemarker.template.TemplateExceptionHandler;
import org.bahmni.batch.exports.MetaDataCodeDictionaryExportStep;
import org.bahmni.batch.exports.NonTBDrugOrderBaseExportStep;
import org.bahmni.batch.exports.ObservationExportStep;
import org.bahmni.batch.exports.TBDrugOrderBaseExportStep;
import org.bahmni.batch.exports.TreatmentRegistrationBaseExportStep;
import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.observation.FormListProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer {

	public static final String FULL_DATA_EXPORT_JOB_NAME = "endtbExports";

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

	@Autowired
	private TBDrugOrderBaseExportStep tbDrugOrderBaseExportStep;

	@Autowired
	private NonTBDrugOrderBaseExportStep nonTBDrugOrderBaseExportStep;

	@Autowired
	private FormListProcessor formListProcessor;

	@Autowired
	private ObjectFactory<ObservationExportStep> observationExportStepFactory;


	@Value("classpath:templates/")
	private Resource freemarkerTemplateLocation;

	@Autowired
	public JobCompletionNotificationListener jobCompletionNotificationListener;

	@Autowired
	private MetaDataCodeDictionaryExportStep metaDataCodeDictionaryExportStep;

	@Bean
	public JobExecutionListener listener() {
		return jobCompletionNotificationListener;
	}

	@Bean
	public Job completeDataExport() throws IOException {

		List<BahmniForm> forms = formListProcessor.retrieveAllForms();

		FlowBuilder<FlowJobBuilder> completeDataExport = jobBuilderFactory.get(FULL_DATA_EXPORT_JOB_NAME)
				.incrementer(new RunIdIncrementer())
				.listener(listener())
		                .flow(treatmentRegistrationBaseExportStep.getStep())
						.next(metaDataCodeDictionaryExportStep.getStep())
		                .next(tbDrugOrderBaseExportStep.getStep())
		                .next(nonTBDrugOrderBaseExportStep.getStep());

		for (BahmniForm form : forms) {
				ObservationExportStep observationExportStep = observationExportStepFactory.getObject();
				observationExportStep.setForm(form);
				completeDataExport.next(observationExportStep.getStep());
		}

		return completeDataExport.end().build();
	}


	@Bean
	public freemarker.template.Configuration freeMarkerConfiguration() throws IOException {
		freemarker.template.Configuration freemarkerTemplateConfig = new freemarker.template.Configuration(
				freemarker.template.Configuration.VERSION_2_3_22);
		freemarkerTemplateConfig.setDirectoryForTemplateLoading(freemarkerTemplateLocation.getFile());
		freemarkerTemplateConfig.setDefaultEncoding("UTF-8");
		freemarkerTemplateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		return freemarkerTemplateConfig;
	}

}
