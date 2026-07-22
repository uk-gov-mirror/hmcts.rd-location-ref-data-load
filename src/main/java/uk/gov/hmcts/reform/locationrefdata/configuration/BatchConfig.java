package uk.gov.hmcts.reform.locationrefdata.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.locationrefdata.camel.listener.JobResultListener;
import uk.gov.hmcts.reform.locationrefdata.camel.task.LrdBuildingLocationRouteTask;
import uk.gov.hmcts.reform.locationrefdata.camel.task.LrdCourtVenueRouteTask;
import uk.gov.hmcts.reform.locationrefdata.camel.task.LrdOrgServiceMappingRouteTask;

@Configuration
@Slf4j
public class BatchConfig {

    @Value("${lrd-route-task}")
    String lrdTask;

    @Value("${lrd-building-location-route-task}")
    String lrdBuildingLocationLoadTask;

    @Value("${lrd-court-venue-route-task}")
    String lrdCourtVenueLoadTask;

    @Value("${batchjob-name}")
    String jobName;

    @Autowired
    LrdOrgServiceMappingRouteTask lrdOrgServiceMappingRouteTask;

    @Autowired
    private LrdBuildingLocationRouteTask lrdBuildingLocationRouteTask;

    @Autowired
    private LrdCourtVenueRouteTask lrdCourtVenueRouteTask;

    @Autowired
    JobResultListener jobResultListener;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Bean
    public Step stepLrdRoute(JobRepository jobRepository) {
        return new StepBuilder(lrdTask, jobRepository)
            .tasklet(lrdOrgServiceMappingRouteTask, transactionManager)
            .build();
    }

    @Bean
    public Step stepLrdBuildingLocationRoute(JobRepository jobRepository) {
        return new StepBuilder(lrdBuildingLocationLoadTask, jobRepository)
            .tasklet(lrdBuildingLocationRouteTask, transactionManager)
            .build();
    }

    @Bean
    public Step stepLrdCourtVenueRoute(JobRepository jobRepository) {
        return new StepBuilder(lrdCourtVenueLoadTask, jobRepository)
            .tasklet(lrdCourtVenueRouteTask, transactionManager)
            .build();
    }

    @Bean
    public Job runRoutesJob(JobRepository jobRepository) {
        return new JobBuilder(jobName, jobRepository)
                .start(stepLrdRoute(jobRepository))
                .listener(jobResultListener)
                .on("*").to(stepLrdBuildingLocationRoute(jobRepository))
                .on("*").to(stepLrdCourtVenueRoute(jobRepository))
                .end()
                .build();
    }
    
}
