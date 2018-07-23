package org.bahmni.mart;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class MartBatchConfigurer extends DefaultBatchConfigurer {

    @Autowired(required = false)
    public void setDataSource(@Qualifier("scdfDb") DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    @Autowired
    public MartBatchConfigurer(@Qualifier("scdfDb") DataSource dataSource) {
        super(dataSource);
    }

    public MartBatchConfigurer() {
    }
}
