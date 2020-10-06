package uk.gov.hmcts.reform.locationrefdata.config;

import org.apache.camel.CamelContext;
import org.apache.camel.component.bean.validator.HibernateValidationProviderResolver;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ArchiveFileProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.FileReadProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.HeaderValidationProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.ArchivalRoute;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.data.ingestion.camel.service.AuditServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.service.EmailServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.service.IEmailService;
import uk.gov.hmcts.reform.data.ingestion.camel.util.DataLoadUtil;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdCaseType;
import uk.gov.hmcts.reform.locationrefdata.camel.listener.JobResultListener;
import uk.gov.hmcts.reform.locationrefdata.camel.mapper.ServiceToCcdCaseTypeMapper;
import uk.gov.hmcts.reform.locationrefdata.camel.processor.ServiceToCcdCaseTypeProcessor;
import uk.gov.hmcts.reform.locationrefdata.camel.task.LrdRouteTask;
import uk.gov.hmcts.reform.locationrefdata.camel.util.LrdExecutor;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.IntegrationTestSupport;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;


@Configuration
public class LrdCamelConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    IntegrationTestSupport integrationTestSupport() {
        return new IntegrationTestSupport();
    }

    @Bean
    public JsrValidatorInitializer<ServiceToCcdCaseType> judicialUserProfileJsrValidatorInitializer() {
        return new JsrValidatorInitializer<>();
    }

    @Bean
    public ServiceToCcdCaseTypeProcessor serviceToCcdServiceProcessor() {
        return new ServiceToCcdCaseTypeProcessor();
    }

    @Bean
    public ServiceToCcdCaseTypeMapper serviceToCcdServiceMapper() {
        return new ServiceToCcdCaseTypeMapper();
    }

    @Bean
    public ServiceToCcdCaseType serviceToCcdService() {

        return new ServiceToCcdCaseType();
    }


    // Route configuration ends

    // processor configuration starts
    @Bean
    FileReadProcessor fileReadProcessor() {
        return new FileReadProcessor();
    }

    @Bean
    ArchiveFileProcessor azureFileProcessor() {
        return new ArchiveFileProcessor();
    }

    @Bean
    public ExceptionProcessor exceptionProcessor() {
        return new ExceptionProcessor();
    }

    @Bean
    public AuditServiceImpl schedulerAuditProcessor() {
        return new AuditServiceImpl();
    }

    @Bean
    public HeaderValidationProcessor headerValidationProcessor() {
        return new HeaderValidationProcessor();
    }
    // processor configuration starts


    // db configuration starts
    private static final PostgreSQLContainer testPostgres = new PostgreSQLContainer("postgres")
        .withDatabaseName("dblrddata_test");

    static {
        testPostgres.start();
    }

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url(testPostgres.getJdbcUrl());
        dataSourceBuilder.username(testPostgres.getUsername());
        dataSourceBuilder.password(testPostgres.getPassword());
        return dataSourceBuilder.build();
    }

    @Bean("springJdbcDataSource")
    public DataSource springJdbcDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url(testPostgres.getJdbcUrl());
        dataSourceBuilder.username(testPostgres.getUsername());
        dataSourceBuilder.password(testPostgres.getPassword());
        return dataSourceBuilder.build();
    }

    @Bean("springJdbcTemplate")
    public JdbcTemplate springJdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(springJdbcDataSource());
        return jdbcTemplate;
    }
    // db configuration ends

    // transaction configuration starts
    @Bean(name = "txManager")
    public PlatformTransactionManager txManager() {
        DataSourceTransactionManager platformTransactionManager = new DataSourceTransactionManager(dataSource());
        platformTransactionManager.setDataSource(dataSource());
        return platformTransactionManager;
    }

    @Bean(name = "springJdbcTransactionManager")
    public PlatformTransactionManager springJdbcTransactionManager() {
        DataSourceTransactionManager platformTransactionManager
            = new DataSourceTransactionManager(springJdbcDataSource());
        platformTransactionManager.setDataSource(springJdbcDataSource());
        return platformTransactionManager;
    }

    @Bean
    public SpringTransactionPolicy getSpringTransaction() {
        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
        springTransactionPolicy.setTransactionManager(txManager());
        springTransactionPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return springTransactionPolicy;
    }
    // transaction configuration ends

    // tasks configuration starts
    @Bean
    LrdRouteTask lrdRouteTask() {
        return new LrdRouteTask();
    }


    @Bean
    LrdExecutor lrdTask() {
        return new LrdExecutor();
    }
    // tasks configuration ends

    // camel related configuration starts
    @Bean
    DataLoadRoute dataLoadRoute() {
        return new DataLoadRoute();
    }

    @Bean
    ArchivalRoute archivalRoute() {
        return new ArchivalRoute();
    }

    @Bean
    public CamelContext camelContext() {
        CamelContext camelContext = new SpringCamelContext(applicationContext);
        return camelContext;
    }
    // camel related configuration ends

    // miscellaneous configuration starts
    @Bean("myValidationProviderResolver")
    HibernateValidationProviderResolver hibernateValidationProviderResolver() {
        return new HibernateValidationProviderResolver();
    }

    @Bean("myConstraintValidatorFactory")
    public ConstraintValidatorFactoryImpl constraintValidatorFactory() {
        return new ConstraintValidatorFactoryImpl();
    }

    @Bean
    public DataLoadUtil dataLoadUtil() {
        return new DataLoadUtil();
    }

    @Bean
    IEmailService emailService() {
        return mock(EmailServiceImpl.class);
    }

    @Bean
    JobResultListener jobResultListener() {
        return new JobResultListener();
    }
    // miscellaneous configuration ends
}
