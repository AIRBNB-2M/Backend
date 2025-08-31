package project.airbnb.clone.common.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import project.airbnb.clone.dto.AccommodationProcessorDto;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AccommodationBatchConfig {

    private final AreaBasedSyncListReader reader;
    private final AccommodationWriter writer;

    private final DetailCommonProcessor commonProcessor;
    private final DetailIntroProcessor introProcessor;
    private final DetailInfoProcessor infoProcessor;

    @Bean
    public ItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> compositeProcessor() {
        CompositeItemProcessor<AccommodationProcessorDto, AccommodationProcessorDto> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(List.of(commonProcessor, introProcessor, infoProcessor));
        return compositeProcessor;
    }

    @Bean
    public Step accommodationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("accommodationStep", jobRepository)
                .<AccommodationProcessorDto, AccommodationProcessorDto>chunk(100, transactionManager)
                .reader(reader)
                .processor(compositeProcessor())
                .writer(writer)
                .build();
    }

    @Bean
    public Job accommodationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("accommodationJob", jobRepository)
                .start(accommodationStep(jobRepository, transactionManager))
                .build();
    }
}
