package uk.gov.hmcts.reform.locationrefdata.cameltest;

import com.google.common.collect.ImmutableList;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdCaseType;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.LrdIntegrationBaseTest;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.RestartingSpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.SpringRestarter;
import uk.gov.hmcts.reform.locationrefdata.config.LrdCamelConfig;
import uk.gov.hmcts.reform.locationrefdata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.sql.Timestamp;

import static org.apache.commons.lang.StringUtils.EMPTY;
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
public class LrdApplicationTest extends LrdIntegrationBaseTest {

    @Value("${start-route}")
    private String startRoute;

    @Value("${archival-route}")
    String archivalRoute;

    @Before
    public void init() {
        jdbcTemplate.execute(truncateAudit);
        SpringRestarter.getInstance().restart();
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletSuccess() throws Exception {
        testInsertion();
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletWithNoInsertionAndDeleteOnDay2() throws Exception {
        testInsertion();
        jdbcTemplate.execute("delete from DATALOAD_SCHEDULAR_AUDIT");
        lrdBlobSupport.uploadFile(
            "service-test.csv",
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test-no-insert-only-delete.csv"))
        );

        Timestamp oldTime = getTime(getCcdCaseTime, "AAA1", "service1");
        producerTemplate.sendBody(startRoute, "retrigger");

        validateLrdServiceFile(jdbcTemplate, lrdSelectData, ImmutableList.of(
            ServiceToCcdCaseType.builder().ccdCaseType("service1")
                .ccdServiceName("ccd-service1").serviceCode("AAA1").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service11")
                .ccdServiceName("ccd-service2").serviceCode("AAA2").build()
        ), 2);

        Timestamp newTime = getTime(getCcdCaseTime, "AAA1", "service1");
        //Validate Existing record not get updated
        assertEquals(oldTime, newTime);
        producerTemplate.sendBody(archivalRoute, "retrigger");
        lrdBlobSupport.deleteBlob("service-test.csv");
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletWithNoInsertionAndUpdateEmptyCaseTypeDay2() throws Exception {
        testInsertion();
        jdbcTemplate.execute("delete from DATALOAD_SCHEDULAR_AUDIT");
        lrdBlobSupport.uploadFile(
            "service-test.csv",
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test-update-empty-casetype.csv"))
        );

        producerTemplate.sendBody(startRoute, "retrigger");

        validateLrdServiceFile(jdbcTemplate, lrdSelectData, ImmutableList.of(
            ServiceToCcdCaseType.builder()
                .ccdServiceName("ccd-service1").serviceCode("AAA1").ccdCaseType(EMPTY).build(),
            ServiceToCcdCaseType.builder()
                .ccdServiceName("ccd-service2").serviceCode("AAA2").ccdCaseType(EMPTY).build()
        ), 2);

        producerTemplate.sendBody(archivalRoute, "retrigger");
        lrdBlobSupport.deleteBlob("service-test.csv");
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletSuccessWithEmptyCaseTypeOrName() throws Exception {
        lrdBlobSupport.uploadFile(
            "service-test.csv",
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test-empty-case-or-name.csv"))
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
                .ccdServiceName("ccd-service2").serviceCode("AAA2").build(),
            ServiceToCcdCaseType.builder()
                .ccdCaseType("service14").serviceCode("AAA4")
                .ccdServiceName(EMPTY).build(),
            ServiceToCcdCaseType.builder()
                .ccdServiceName("ccd-service3").serviceCode("AAA3")
                .ccdCaseType(EMPTY).build()
        ), 6);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success");
        //Delete Uploaded test file with Snapshot delete
        lrdBlobSupport.deleteBlob("service-test.csv");
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletSuccessWithUpdateAndDelete() throws Exception {

        testInsertion();
        jdbcTemplate.execute("delete from DATALOAD_SCHEDULAR_AUDIT");
        lrdBlobSupport.uploadFile(
            "service-test.csv",
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test-delete-insert.csv"))
        );

        producerTemplate.sendBody(startRoute, "retrigger");

        validateLrdServiceFile(jdbcTemplate, lrdSelectData, ImmutableList.of(
            ServiceToCcdCaseType.builder().ccdCaseType("service1")
                .ccdServiceName("ccd-service1").serviceCode("AAA1").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service3")
                .ccdServiceName("ccd-service1").serviceCode("AAA1").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service15")
                .ccdServiceName("ccd-service2").serviceCode("AAA2").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service16")
                .ccdServiceName("ccd-service2").serviceCode("AAA2").build()
        ), 4);

        producerTemplate.sendBody(archivalRoute, "retrigger");
        lrdBlobSupport.deleteBlob("service-test.csv");
    }

    private void testInsertion() throws Exception {
        lrdBlobSupport.uploadFile(
            "service-test.csv",
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
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success");
        //Delete Uploaded test file with Snapshot delete
        lrdBlobSupport.deleteBlob("service-test.csv");
    }


}
