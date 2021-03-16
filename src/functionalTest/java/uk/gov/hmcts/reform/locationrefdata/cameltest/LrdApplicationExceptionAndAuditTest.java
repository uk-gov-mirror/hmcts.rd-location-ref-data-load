package uk.gov.hmcts.reform.locationrefdata.cameltest;

import com.google.common.collect.ImmutableList;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdCaseType;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.LrdIntegrationBaseTest;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.SpringStarter;
import uk.gov.hmcts.reform.locationrefdata.config.LrdCamelConfig;
import uk.gov.hmcts.reform.locationrefdata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.util.Date;

import static org.javatuples.Triplet.with;
import static org.junit.Assert.assertEquals;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.SCHEDULER_START_TIME;

@TestPropertySource(properties = {"spring.config.location=classpath:application-integration.yml,"
    + "classpath:application-leaf-integration.yml"})

@CamelSpringBootTest
@MockEndpoints("log:*")
@ContextConfiguration(classes = {LrdCamelConfig.class, CamelTestContextBootstrapper.class,
    JobLauncherTestUtils.class, BatchConfig.class, AzureBlobConfig.class, BlobStorageCredentials.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
@EnableTransactionManagement
@SqlConfig(dataSource = "dataSource", transactionManager = "txManager",
    transactionMode = SqlConfig.TransactionMode.ISOLATED)
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@SuppressWarnings("unchecked")
class LrdApplicationExceptionAndAuditTest extends LrdIntegrationBaseTest {

    @BeforeEach
    public void init() {
        SpringStarter.getInstance().restart();
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletPartialSuccessAndJsr() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test-partial-success.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdServiceFile(jdbcTemplate, lrdSelectData, ImmutableList.of(
            ServiceToCcdCaseType.builder().ccdCaseType("service1")
                .ccdServiceName("ccd-service1").serviceCode("AAA1").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service2")
                .ccdServiceName("ccd-service1").serviceCode("AAA1").build()
        ), 2);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "PartialSuccess", UPLOAD_FILE_NAME);
        Triplet<String, String, String> triplet = with("serviceCode", "must not be blank", "");
        validateLrdServiceFileJsrException(jdbcTemplate, exceptionQuery, 1, triplet);
        //Delete Uploaded test file with Snapshot delete
        lrdBlobSupport.deleteBlob(UPLOAD_FILE_NAME);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    void testTaskletFailure() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test-failure.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var serviceToCcdServices = jdbcTemplate.queryForList(lrdSelectData);
        assertEquals(serviceToCcdServices.size(), 0);

        Pair<String, String> pair = new Pair<>(
            UPLOAD_FILE_NAME,
            "ServiceToCcdService failed as no valid records present"
        );
        validateLrdServiceFileException(jdbcTemplate, exceptionQuery, pair);
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_FILE_NAME);
        lrdBlobSupport.deleteBlob(UPLOAD_FILE_NAME);
    }

    private void testInsertion() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdServiceFile(jdbcTemplate, lrdSelectData, ImmutableList.of(
            ServiceToCcdCaseType.builder().ccdCaseType("service1")
                .ccdServiceName("ccd-service1").serviceCode("AAA1").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service2")
                .ccdServiceName("ccd-service1").serviceCode("AAA1").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service11")
                .ccdServiceName("ccd-service2").serviceCode("AAA2").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service12")
                .ccdServiceName("ccd-service2").serviceCode("AAA2").build()
        ), 4);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_FILE_NAME);
        //Delete Uploaded test file with Snapshot delete
        lrdBlobSupport.deleteBlob(UPLOAD_FILE_NAME);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    void testTaskletFailureForInvalidService() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test-invalid-service-failure.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var serviceToCcdServices = jdbcTemplate.queryForList(lrdSelectData);
        assertEquals(serviceToCcdServices.size(), 0);

        Pair<String, String> pair = new Pair<>(
            UPLOAD_FILE_NAME,
            "violates foreign key constraint"
        );
        validateLrdServiceFileException(jdbcTemplate, exceptionQuery, pair);
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_FILE_NAME);
        lrdBlobSupport.deleteBlob(UPLOAD_FILE_NAME);
    }
}
