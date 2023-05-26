package com.example.batch;

import com.example.batch.entity.Alarm;
import com.example.batch.entity.Coffee;
import com.example.batch.entity.DayOfWeek;
import com.example.batch.processor.AlarmItemProcessor;
import com.example.batch.processor.CoffeeItemProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class BatchConfiguration {
    private final DataSource dataSource;
    private final AlarmRepository alarmRepository;
    @Value("${file.input}")
    private String fileInput;

    @Bean
    public FlatFileItemReader<Coffee> coffeeReader() {
        return new FlatFileItemReaderBuilder<Coffee>().name("coffeeItemReader")
                .resource(new ClassPathResource(fileInput))
                .delimited()
                .names(new String[] { "brand", "origin", "characteristics" })
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Coffee>() {{
                    setTargetType(Coffee.class);
                }})
                .build();
    }

    @Bean
    public JdbcCursorItemReader<DayOfWeek> reader() {
        JdbcCursorItemReader<DayOfWeek> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("select alarm_id, day from day_of_week");
        reader.setRowMapper(new DayOfWeekRowMapper(alarmRepository));
//        reader.setMaxRows(10);
//        reader.setFetchSize(10);
        reader.setQueryTimeout(10000);
        return reader;
    }

    @Bean
    public FlatFileItemWriter<Alarm> flatWriter() {
        FlatFileItemWriter<Alarm> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("C://data/batch/data.csv"));
        writer.setLineAggregator(getDelimitedLineAggregator());
        writer.close();
        return writer;
    }
    private DelimitedLineAggregator<Alarm> getDelimitedLineAggregator() {
        BeanWrapperFieldExtractor<Alarm> beanWrapperFieldExtractor = new BeanWrapperFieldExtractor<Alarm>();
        beanWrapperFieldExtractor.setNames(new String[]{"id"});

        DelimitedLineAggregator<Alarm> aggregator = new DelimitedLineAggregator<Alarm>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(beanWrapperFieldExtractor);
        return aggregator;

    }

    @Bean
    public CoffeeItemProcessor processor() {
        return new CoffeeItemProcessor();
    }

    @Bean
    AlarmItemProcessor alarmItemProcessor() {
        return new AlarmItemProcessor(alarmRepository);
    }

    @Bean
    public JdbcBatchItemWriter<Coffee> writer() {
        return new JdbcBatchItemWriterBuilder<Coffee>().itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO coffee (brand, origin, characteristics) VALUES (:brand, :origin, :characteristics)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository, JobCompletionNotificationListener listener, Step step1) {
        return new JobBuilder("importUserJob", jobRepository)
                 .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .<DayOfWeek, Alarm> chunk(10, transactionManager)
                .reader(reader())
                .writer(new NoOpItemWriter())
                .processor(alarmItemProcessor())
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step", jobRepository)
                .<Coffee, Coffee> chunk(10, transactionManager)
                .reader(coffeeReader())
                .writer(writer())
                .processor(processor())
                .build();
    }

}