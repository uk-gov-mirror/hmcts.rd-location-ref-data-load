package uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.BeforeClass;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.data.ingestion.camel.service.IEmailService;
import uk.gov.hmcts.reform.data.ingestion.camel.util.DataLoadUtil;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdCaseType;

import java.sql.Timestamp;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.jdbc.core.BeanPropertyRowMapper.newInstance;

public abstract class LrdIntegrationBaseTest {

    public static final String DB_SCHEDULER_STATUS = "scheduler_status";

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected DataLoadRoute parentRoute;

    @Value("${start-route}")
    protected String startRoute;

    @Autowired
    protected ProducerTemplate producerTemplate;

    @Value("${archival-cred}")
    protected String archivalCred;

    @Value("${lrd-select-sql}")
    protected String lrdSelectData;

    @Value("${audit-enable}")
    protected Boolean auditEnable;

    @Autowired
    protected DataLoadUtil dataLoadUtil;

    @Autowired
    protected ExceptionProcessor exceptionProcessor;

    @Autowired
    protected IEmailService emailService;

    @Autowired
    protected JobLauncherTestUtils jobLauncherTestUtils;

    @Value("${exception-select-query}")
    protected String exceptionQuery;

    @Value("${truncate-audit}")
    protected String truncateAudit;

    @Value("${select-dataload-scheduler}")
    protected String auditSchedulerQuery;

    @Value("${get-ccd-case-time}")
    protected String getCcdCaseTime;

    @Autowired
    protected LrdBlobSupport lrdBlobSupport;

    @BeforeClass
    public static void beforeAll() throws Exception {
        if ("preview".equalsIgnoreCase(System.getenv("execution_environment"))) {
            System.setProperty("azure.storage.account-key", System.getenv("ACCOUNT_KEY_PREVIEW"));
            System.setProperty("azure.storage.account-name", "rdpreview");
        } else {
            System.setProperty("azure.storage.account-key", System.getenv("ACCOUNT_KEY"));
            System.setProperty("azure.storage.account-name", System.getenv("ACCOUNT_NAME"));
        }
    }

    protected void validateLrdServiceFile(JdbcTemplate jdbcTemplate, String serviceSql,
                                          List<ServiceToCcdCaseType> exceptedResult, int size) {
        var rowMapper = newInstance(ServiceToCcdCaseType.class);
        var serviceToCcdServices = jdbcTemplate.query(serviceSql, rowMapper);
        assertEquals(size, serviceToCcdServices.size());
        assertEquals(exceptedResult, serviceToCcdServices);
    }


    protected void validateLrdServiceFileAudit(JdbcTemplate jdbcTemplate,
                                               String auditSchedulerQuery, String status) {
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).get("scheduler_status"));
    }

    @SuppressWarnings("unchecked")
    protected void validateLrdServiceFileJsrException(JdbcTemplate jdbcTemplate,
                                                      String exceptionQuery, int size,
                                                      Triplet<String, String, String>... triplets) {
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertEquals(result.size(), size);
        for (Triplet triplet : triplets) {
            int index = 0;
            assertEquals(triplet.getValue0(), result.get(index).get("field_in_error"));
            assertEquals(triplet.getValue1(), result.get(index).get("error_description"));
            assertEquals(triplet.getValue2(), result.get(index).get("key"));
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    protected void validateLrdServiceFileException(JdbcTemplate jdbcTemplate,
                                                   String exceptionQuery,
                                                   Pair<String, String> pair) {
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertEquals(pair.getValue0(), result.get(result.size() - 1).get("file_name"));
        assertThat(
            (String) result.get(result.size() - 1).get("error_description"),
            containsString(pair.getValue1())
        );
    }

    protected Timestamp getTime(String sql, String serviceCode, String caseType) {
        return jdbcTemplate.queryForObject(sql, new Object[]{serviceCode, caseType}, Timestamp.class);
    }
}
