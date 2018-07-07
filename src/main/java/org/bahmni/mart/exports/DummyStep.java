package org.bahmni.mart.exports;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DummyStep {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    public Step getStep() {
        return stepBuilderFactory
                .get("Dummy Step")
                .<String, String>chunk(50)
                .reader(new DummyItemReader())
                .writer(new DummyItemWriter())
                .build();
    }


    private class DummyItemReader implements ItemReader {
        @Override
        public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
            return null;
        }
    }

    private class DummyItemWriter implements ItemWriter {
        @Override
        public void write(List items) throws Exception {

        }
    }
}
