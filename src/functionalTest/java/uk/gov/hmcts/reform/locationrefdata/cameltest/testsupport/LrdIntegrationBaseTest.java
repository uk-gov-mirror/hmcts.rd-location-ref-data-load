package uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport;

import org.apache.camel.CamelContext;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.data.ingestion.DataIngestionLibraryRunner;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ArchiveFileProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.data.ingestion.camel.service.AuditServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.service.IEmailService;
import uk.gov.hmcts.reform.data.ingestion.camel.util.DataLoadUtil;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.CourtVenue;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdCaseType;
import uk.gov.hmcts.reform.locationrefdata.camel.task.LrdOrgServiceMappingRouteTask;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.jdbc.core.BeanPropertyRowMapper.newInstance;

@ExtendWith(SpringExtension.class)
public abstract class LrdIntegrationBaseTest {

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    @Qualifier("springJdbcTemplate")
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected DataLoadRoute parentRoute;

    @Value("${start-route}")
    protected String startRoute;


    @Value("${archival-cred}")
    protected String archivalCred;

    @Value("${lrd-select-sql}")
    protected String lrdSelectData;

    @Value("${lrd-court-venue-select-sql}")
    protected String lrdCourtVenueSelectData;

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

    @Value("${ordered-exception-select-query}")
    protected String orderedExceptionQuery;

    @Value("${select-dataload-scheduler}")
    protected String auditSchedulerQuery;

    @Autowired
    protected LrdBlobSupport lrdBlobSupport;

    @Autowired
    protected DataIngestionLibraryRunner dataIngestionLibraryRunner;

    @Autowired
    protected AuditServiceImpl auditService;

    @Autowired
    protected ArchiveFileProcessor archiveFileProcessor;

    @Autowired
    protected LrdOrgServiceMappingRouteTask lrdOrgServiceMappingRouteTask;

    public static final String UPLOAD_ORG_SERVICE_FILE_NAME = "service-test.csv";
    public static final String UPLOAD_COURT_FILE_NAME = "court-venue-test.csv";

    @BeforeEach
    public void setUpSpringContext() throws Exception {
        new TestContextManager(getClass()).prepareTestInstance(this);
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);
        SpringStarter.getInstance().init(testContextManager);
    }


    @BeforeAll
    public static void beforeAll() {
        if ("preview".equalsIgnoreCase(System.getenv("execution_environment"))) {
            System.setProperty("azure.storage.account-key", System.getenv("BLOB_ACCOUNT_KEY"));
            System.setProperty("azure.storage.account-name", System.getenv("BLOB_ACCOUNT_NAME"));
        } else {
            System.setProperty("azure.storage.account-key", System.getenv("ACCOUNT_KEY"));
            System.setProperty("azure.storage.account-name", System.getenv("ACCOUNT_NAME"));
        }
        System.setProperty("azure.storage.container-name", "lrd-ref-data");

    }

    protected void validateLrdServiceFile(JdbcTemplate jdbcTemplate, String serviceSql,
                                          List<ServiceToCcdCaseType> exceptedResult, int size) {
        var rowMapper = newInstance(ServiceToCcdCaseType.class);
        var serviceToCcdServices = jdbcTemplate.query(serviceSql, rowMapper);
        assertEquals(size, serviceToCcdServices.size());
        assertEquals(exceptedResult, serviceToCcdServices);
    }

    protected void validateLrdCourtVenueFile(JdbcTemplate jdbcTemplate, String courtVenueSql,
                                             List<CourtVenue> expectedResult, int size) {
        var rowMapper = newInstance(CourtVenue.class);
        var courtVenues = jdbcTemplate.query(courtVenueSql, rowMapper);
        assertEquals(size, courtVenues.size());
        courtVenues.forEach(this::processCourtVenue);
        assertEquals(expectedResult, courtVenues);
    }

    private void processCourtVenue(CourtVenue courtVenue) {
        if (courtVenue.getOpenForPublic().equalsIgnoreCase("t")) {
            courtVenue.setOpenForPublic("Yes");
        } else {
            courtVenue.setOpenForPublic("No");
        }
    }


    protected void validateLrdServiceFileAudit(JdbcTemplate jdbcTemplate,
                                               String auditSchedulerQuery, String status, String fileName) {
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(3, result.size());
        Optional<Map<String, Object>> auditEntry =
            result.stream().filter(audit -> audit.containsValue(fileName)).findFirst();
        assertTrue(auditEntry.isPresent());
        auditEntry.ifPresent(audit -> {
            assertEquals(status, audit.get("status"));
        });
    }

    protected void validateLrdCourtVenueFileForUtfHeader(JdbcTemplate jdbcTemplate, String courtVenueSql,
                                                   List<CourtVenue> expectedResult, int size) {

        var result = jdbcTemplate.queryForList(courtVenueSql);
        var rowMapper = newInstance(CourtVenue.class);

        assertEquals(size, result.size());
    }

    @SuppressWarnings("unchecked")
    protected void validateLrdServiceFileJsrException(JdbcTemplate jdbcTemplate,
                                                      String exceptionQuery, int size, String tableName,
                                                      Quartet<String, String, String, Long>... quartets) {
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertEquals(result.size(), size);

        List<Map<String, Object>> actualResult = result.stream()
            .filter(exception -> exception.containsValue(tableName))
            .collect(Collectors.toUnmodifiableList());
        int numberOfMatchingErrors = 0;
        for (Map<String, Object> currResult: actualResult) {
            for (Quartet<String, String, String, Long> quartet : quartets) {
                if (quartet.getValue1().equals(currResult.get("error_description"))) {
                    numberOfMatchingErrors++;
                    assertEquals(quartet.getValue0(), currResult.get("field_in_error"));
                    assertEquals(quartet.getValue2(), currResult.get("key"));
                    assertEquals(quartet.getValue3(), currResult.get("row_id"));
                }
            }
        }
        assertEquals(numberOfMatchingErrors, quartets.length);
    }

    @SuppressWarnings("unchecked")
    protected void validateLrdServiceFileException(JdbcTemplate jdbcTemplate,
                                                   String exceptionQuery,
                                                   Pair<String, String> pair,
                                                   int index) {
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertThat(
            (String) result.get(index).get("error_description"),
            containsString(pair.getValue1())
        );
    }


    protected Timestamp getTime(String sql, String serviceCode, String caseType) {

        return jdbcTemplate.queryForObject(sql, Timestamp.class, new Object[]{serviceCode, caseType});
    }
}
