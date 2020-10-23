package uk.gov.hmcts.reform.locationrefdata.cameltest;

import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.MockEndpoints;
import org.javatuples.Pair;
import org.junit.Before;
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
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.LrdIntegrationBaseTest;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.RestartingSpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.SpringRestarter;
import uk.gov.hmcts.reform.locationrefdata.config.LrdCamelConfig;
import uk.gov.hmcts.reform.locationrefdata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.SCHEDULER_START_TIME;

@TestPropertySource(properties = {"spring.config.location=classpath:application-integration.yml,"
    + "classpath:application-leaf-integration.yml"})
@RunWith(RestartingSpringJUnit4ClassRunner.class)
@MockEndpoints("log:*")
@ContextConfiguration(classes = {LrdCamelConfig.class, CamelTestContextBootstrapper.class,
    JobLauncherTestUtils.class, BatchConfig.class, AzureBlobConfig.class, BlobStorageCredentials.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
@EnableTransactionManagement
@SqlConfig(dataSource = "dataSource", transactionManager = "txManager",
    transactionMode = SqlConfig.TransactionMode.ISOLATED)
public class LrdFileStatusCheckTest extends LrdIntegrationBaseTest {

    @Before
    public void init() {
        SpringRestarter.getInstance().restart();
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletStaleFileError() throws Exception {

        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(
                new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000).getTime()));

        lrdBlobSupport.uploadFile(
            "service-test.csv",
            new FileInputStream(getFile(
                "classpath:sourceFiles/service-test.csv"))
        );

        jobLauncherTestUtils.launchJob();
        Pair<String, String> pair = new Pair<>(
            "service-test.csv",
            "not loaded due to file stale error"
        );
        validateLrdServiceFileException(jdbcTemplate, exceptionQuery, pair);
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(0, result.size());
        lrdBlobSupport.deleteBlob("service-test.csv", false);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd.sql"})
    public void testTaskletNoFileError() throws Exception {
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));

        jobLauncherTestUtils.launchJob();
        Pair<String, String> pair = new Pair<>(
            "service-test.csv",
            "service-test.csv file is not exists in container"
        );
        validateLrdServiceFileException(jdbcTemplate, exceptionQuery, pair);
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(0, result.size());
    }
}
