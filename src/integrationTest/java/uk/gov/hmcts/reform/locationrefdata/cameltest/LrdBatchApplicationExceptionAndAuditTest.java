package uk.gov.hmcts.reform.locationrefdata.cameltest;

import com.google.common.collect.ImmutableList;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.MockEndpoints;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.StorageCredentials;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdService;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.LrdBatchIntegrationSupport;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.RestartingSpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.SpringRestarter;
import uk.gov.hmcts.reform.locationrefdata.config.LrdCamelConfig;
import uk.gov.hmcts.reform.locationrefdata.configuration.BatchConfig;

import java.io.FileInputStream;

import static org.javatuples.Triplet.with;
import static org.junit.Assert.assertEquals;
import static org.springframework.util.ResourceUtils.getFile;

@TestPropertySource(properties = {"spring.config.location=classpath:application-integration.yml,"
    + "classpath:application-leaf-integration.yml"})
@RunWith(RestartingSpringJUnit4ClassRunner.class)
@MockEndpoints("log:*")
@ContextConfiguration(classes = {LrdCamelConfig.class, CamelTestContextBootstrapper.class,
    JobLauncherTestUtils.class, BatchConfig.class, AzureBlobConfig.class, StorageCredentials.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
@EnableTransactionManagement
@SqlConfig(dataSource = "dataSource", transactionManager = "txManager",
    transactionMode = SqlConfig.TransactionMode.ISOLATED)
@SuppressWarnings("unchecked")
public class LrdBatchApplicationExceptionAndAuditTest extends LrdBatchIntegrationSupport {

    @Before
    public void init() {
        jdbcTemplate.execute(truncateAudit);
        SpringRestarter.getInstance().restart();
    }



    @BeforeClass
    public static void beforeAll() throws Exception {
        if ("preview".equalsIgnoreCase(System.getenv("execution_environment"))) {
            System.setProperty("ACCOUNT_KEY", System.getenv("ACCOUNT_KEY_PREVIEW"));
            System.setProperty("ACCOUNT_NAME", "rdpreview");
        }
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletPartialSuccessAndJsr() throws Exception {
        integrationTestSupport.uploadFile(
            "service-test.csv",
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test-partial-success.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdServiceFile(jdbcTemplate, lrdSelectData, ImmutableList.of(
            ServiceToCcdService.builder().ccdServiceName("service1")
                .ccdJurisdictionName("test-jurisdiction1").serviceCode("AAA1").build(),
            ServiceToCcdService.builder().ccdServiceName("service2")
                .ccdJurisdictionName("test-jurisdiction1").serviceCode("AAA1").build()
        ), 2);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "PartialSuccess");
        Triplet<String, String, String> triplet = with("ccdServiceName", "must not be empty", "AAA2");
        validateLrdServiceFileJsrException(jdbcTemplate, exceptionQuery, 1, triplet);
        //Delete Uploaded test file with Snapshot delete
        integrationTestSupport.deleteBlob("service-test.csv");
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletFailure() throws Exception {
        integrationTestSupport.uploadFile(
            "service-test.csv",
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test-failure.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var serviceToCcdServices = jdbcTemplate.queryForList(lrdSelectData);
        assertEquals(serviceToCcdServices.size(), 0);

        Pair<String, String> pair = new Pair<>(
            "service-test.csv",
            "ServiceToCcdService failed as no valid records present"
        );
        validateLrdServiceFileException(jdbcTemplate, exceptionQuery, pair);
        integrationTestSupport.deleteBlob("service-test.csv");
    }

    private void testInsertion() throws Exception {
        integrationTestSupport.uploadFile(
            "service-test.csv",
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdServiceFile(jdbcTemplate, lrdSelectData, ImmutableList.of(
            ServiceToCcdService.builder().ccdServiceName("service1")
                .ccdJurisdictionName("test-jurisdiction1").serviceCode("AAA1").build(),
            ServiceToCcdService.builder().ccdServiceName("service2")
                .ccdJurisdictionName("test-jurisdiction1").serviceCode("AAA1").build(),
            ServiceToCcdService.builder().ccdServiceName("service11")
                .ccdJurisdictionName("test-jurisdiction2").serviceCode("AAA2").build(),
            ServiceToCcdService.builder().ccdServiceName("service12")
                .ccdJurisdictionName("test-jurisdiction2").serviceCode("AAA2").build()
        ), 4);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success");
        //Delete Uploaded test file with Snapshot delete
        integrationTestSupport.deleteBlob("service-test.csv");
    }
}
