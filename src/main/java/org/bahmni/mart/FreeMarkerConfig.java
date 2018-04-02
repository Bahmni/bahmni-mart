package org.bahmni.mart;

import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FreeMarkerConfig {
    private static final String DEFAULT_ENCODING = "UTF-8";

    @Bean
    public freemarker.template.Configuration freeMarkerConfiguration() {
        freemarker.template.Configuration freemarkerTemplateConfig = new freemarker.template.Configuration(
                freemarker.template.Configuration.VERSION_2_3_22);
        freemarkerTemplateConfig.setClassForTemplateLoading(this.getClass(), "/templates");
        freemarkerTemplateConfig.setDefaultEncoding(DEFAULT_ENCODING);
        freemarkerTemplateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return freemarkerTemplateConfig;
    }

}
