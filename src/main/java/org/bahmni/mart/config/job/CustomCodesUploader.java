package org.bahmni.mart.config.job;

import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomCodesUploader extends CSVUploader {

    @Autowired
    private ObjectFactory<CustomCodesTasklet> objectFactory;

    @Override
    public Tasklet getCSVUploaderTasklet(String readerFilePath) {
        CustomCodesTasklet customCodesTasklet = objectFactory.getObject();
        customCodesTasklet.setReaderFilePath(readerFilePath);
        return customCodesTasklet;
    }
}
