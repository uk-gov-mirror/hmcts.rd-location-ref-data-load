package uk.gov.hmcts.reform.locationrefdata.cameltest;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.hamcrest.MatcherAssert;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.CourtVenue;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.LrdIntegrationBaseTest;
import uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport.SpringStarter;
import uk.gov.hmcts.reform.locationrefdata.config.LrdCamelConfig;
import uk.gov.hmcts.reform.locationrefdata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.javatuples.Quartet.with;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.SCHEDULER_START_TIME;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.CLUSTER_ID;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.CLUSTER_ID_NOT_EXISTS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.COURT_TYPE_ID;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.COURT_TYPE_ID_NOT_EXISTS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.EPIMMS_ID;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.EPIMMS_ID_NOT_EXISTS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.INVALID_EPIMS_ID;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.REGION_ID;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.REGION_ID_NOT_EXISTS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.SERVICE_CODE;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.SERVICE_CODE_NOT_EXISTS;

@TestPropertySource(properties = "spring.config.location=classpath:application-integration.yml")
@CamelSpringBootTest
@MockEndpoints("log:*")
@ContextConfiguration(classes = {LrdCamelConfig.class, CamelTestContextBootstrapper.class,
    BatchConfig.class, AzureBlobConfig.class, BlobStorageCredentials.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@SpringBatchTest
@SpringBootTest
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
@EnableTransactionManagement
@SqlConfig(dataSource = "dataSource", transactionManager = "txManager",
    transactionMode = SqlConfig.TransactionMode.ISOLATED)
@SuppressWarnings("unchecked")
public class LrdCourtVenueTest extends LrdIntegrationBaseTest {

    private static final String COURT_VENUE_TABLE_NAME = "court_venue";
    private static final String COURT_VENUE_LOCATION_DETAILS_SQL = """
        SELECT cv.court_type_id,
               details.venue_name,
               details.is_case_management_location,
               details.is_hearing_location,
               details.welsh_venue_name,
               details.is_temporary_location,
               details.is_nightingale_court,
               details.location_type,
               details.parent_location
        FROM court_venue_location_details details
        JOIN court_venue cv ON cv.court_venue_id = details.court_venue_id
        ORDER BY cv.court_type_id
        """;
    private static final String COURT_VENUE_REFERENCE_DETAILS_SQL = """
        SELECT cv.court_type_id,
               reference.welsh_court_name,
               reference.uprn,
               reference.venue_ou_code,
               reference.mrd_building_location_id,
               reference.mrd_venue_id,
               reference.service_url,
               reference.fact_url,
               reference.external_short_name,
               reference.welsh_external_short_name
        FROM court_venue_reference_details reference
        JOIN court_venue cv ON cv.court_venue_id = reference.court_venue_id
        ORDER BY cv.court_type_id
        """;

    @Autowired
    @Qualifier("springJdbcTransactionManager")
    protected PlatformTransactionManager platformTransactionManager;

    @BeforeEach
    public void init() {
        SpringStarter.getInstance().restart();
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTaskletSuccess() throws Exception {
        testCourtVenueInsertion();
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTaskletSuccessExternal() throws Exception {
        testCourtVenueExternalCourtName();
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTaskletSuccessWelshExternal() throws Exception {
        testCourtVenueWelshExternalShortName();
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTaskletSuccessServiceCode() throws Exception {
        testCourtVenueServiceCode();
    }

    private void testCourtVenueServiceCode() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-test-service_code.csv"))
        );
        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("B Tribunal Hearing Centre")
                .courtName("B TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .phoneNumber("").closedDate(null).courtLocationCode("").dxAddress("").welshSiteName("")
                .welshCourtAddress("").venueName("").isCaseManagementLocation("").isHearingLocation("")
                .welshVenueName("testVenue1").isTemporaryLocation("N").isNightingaleCourt("N").locationType("Court")
                .parentLocation("366559").welshCourtName("testWelshCourtName").uprn("uprn123")
                .venueOuCode("venueOuCode1").mrdBuildingLocationId("mrdBId1")
                .mrdVenueId("mrdVenueId1").serviceUrl("serviceUrl1").factUrl("factUrl1")
                .mrdDeletedTime("2022-04-01 02:00:03")
                .externalShortName("External Short Name")
                .welshExternalShortName("Welsh External Short Name").serviceCode("AAA1").build()
        ), 1);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_COURT_FILE_NAME);
    }

    private void testCourtVenueInsertion() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-test.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("A Tribunal Hearing Centre")
                .courtName("A TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .phoneNumber("").closedDate(null).courtLocationCode("").dxAddress("").welshSiteName("")
                .welshCourtAddress("").venueName("").isCaseManagementLocation("").isHearingLocation("")
                .welshVenueName("testVenue1").isTemporaryLocation("N").isNightingaleCourt("N").locationType("Court")
                .parentLocation("366559").welshCourtName("testWelshCourtName").uprn("uprn123")
                .venueOuCode("venueOuCode1").mrdBuildingLocationId("mrdBId1")
                .mrdVenueId("mrdVenueId1").serviceUrl("serviceUrl1").factUrl("factUrl1")
                .mrdCreatedTime("2022-04-01 02:00:01").mrdUpdatedTime("2022-04-01 02:00:02")
                .mrdDeletedTime("2022-04-01 02:00:03").build(),
            CourtVenue.builder().epimmsId("123456").siteName("B Tribunal Hearing Centre")
                .courtName("B TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("31")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .phoneNumber("").closedDate(null).courtLocationCode("").dxAddress("").welshSiteName("")
                .welshCourtAddress("").venueName("").isCaseManagementLocation("").isHearingLocation("")
                .welshVenueName("testVenue2").isTemporaryLocation("N").isNightingaleCourt("N").locationType("Court")
                .parentLocation("372653").welshCourtName("testWelshCourtName").uprn("uprn123")
                .venueOuCode("venueOuCode1").mrdBuildingLocationId("mrdBId1")
                .mrdVenueId("mrdVenueId1").serviceUrl("serviceUrl1").factUrl("factUrl1").build()
        ), 2);
        validateChildTables(2);
        assertLocationValue("17", "welsh_venue_name", "testVenue1");
        assertLocationValue("17", "parent_location", "366559");
        assertLocationValue("31", "welsh_venue_name", "testVenue2");
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_COURT_FILE_NAME);
    }

    private void testCourtVenueExternalCourtName() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-test-external-court-name.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("A Tribunal Hearing Centre")
                .courtName("A TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .phoneNumber("").closedDate(null).courtLocationCode("").dxAddress("").welshSiteName("")
                .welshCourtAddress("").venueName("").isCaseManagementLocation("").isHearingLocation("")
                .welshVenueName("testVenue1").isTemporaryLocation("N").isNightingaleCourt("N").locationType("Court")
                .parentLocation("366559").welshCourtName("testWelshCourtName").uprn("uprn123")
                .venueOuCode("venueOuCode1").mrdBuildingLocationId("mrdBId1")
                .mrdVenueId("mrdVenueId1").serviceUrl("serviceUrl1").factUrl("factUrl1")
                .mrdCreatedTime("2022-04-01 02:00:01").mrdUpdatedTime("2022-04-01 02:00:02")
                .mrdDeletedTime("2022-04-01 02:00:03")
                .externalShortName("A TRIBUNAL HEARING CENTRE External").build(),
            CourtVenue.builder().epimmsId("123456").siteName("B Tribunal Hearing Centre")
                .courtName("B TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("31")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .phoneNumber("").closedDate(null).courtLocationCode("").dxAddress("").welshSiteName("")
                .welshCourtAddress("").venueName("").isCaseManagementLocation("").isHearingLocation("")
                .welshVenueName("testVenue2").isTemporaryLocation("N").isNightingaleCourt("N").locationType("Court")
                .parentLocation("372653").welshCourtName("testWelshCourtName").uprn("uprn123")
                .venueOuCode("venueOuCode1").mrdBuildingLocationId("mrdBId1")
                .mrdVenueId("mrdVenueId1").serviceUrl("serviceUrl1").factUrl("factUrl1")
                .externalShortName("A TRIBUNAL HEARING CENTRE External").build()
        ), 2);
        validateChildTables(2);
        assertReferenceValue("17", "external_short_name", "A TRIBUNAL HEARING CENTRE External");
        assertReferenceValue("31", "external_short_name", "A TRIBUNAL HEARING CENTRE External");
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_COURT_FILE_NAME);
    }

    private void testCourtVenueWelshExternalShortName() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-test-welsh-external-court-name.csv"))
        );
        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("B Tribunal Hearing Centre")
                .courtName("B TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .phoneNumber("").closedDate(null).courtLocationCode("").dxAddress("").welshSiteName("")
                .welshCourtAddress("").venueName("").isCaseManagementLocation("").isHearingLocation("")
                .welshVenueName("testVenue1").isTemporaryLocation("N").isNightingaleCourt("N").locationType("Court")
                .parentLocation("366559").welshCourtName("testWelshCourtName").uprn("uprn123")
                .venueOuCode("venueOuCode1").mrdBuildingLocationId("mrdBId1")
                .mrdVenueId("mrdVenueId1").serviceUrl("serviceUrl1").factUrl("factUrl1")
                .mrdDeletedTime("2022-04-01 02:00:03")
                .externalShortName("External Short Name")
                .welshExternalShortName("Welsh External Short Name").serviceCode("AAA1").build()
        ), 1);
        validateChildTables(1);
        assertReferenceValue("17", "external_short_name", "External Short Name");
        assertReferenceValue("17", "welsh_external_short_name", "Welsh External Short Name");
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_COURT_FILE_NAME);
    }


    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTaskletPartialSuccess() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-partial-success.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("A Tribunal Hearing Centre")
                .courtName("A TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .build()
        ), 1);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "PartialSuccess", UPLOAD_COURT_FILE_NAME);
        Quartet<String, String, String, Long> quartet1 =
            Quartet.with("epimmsId", INVALID_EPIMS_ID, "", 3L);
        Quartet<String, String, String, Long> quartet2 = Quartet.with("epimmsId", "must not be blank", "", 3L);
        validateLrdServiceFileJsrException(jdbcTemplate, orderedExceptionQuery, 4,
                                           COURT_VENUE_TABLE_NAME, quartet1, quartet2);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void test_court_venue_with_unicode_header() throws Exception {
        String fileName = "court-venue-utf8-header-success.csv";

        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                String.format("classpath:sourceFiles/courtVenues/%s", fileName)))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFileForUtfHeader(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("A Tribunal Hearing Centre")
                .courtName("A TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .build()
        ), 1);

        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "PartialSuccess", UPLOAD_COURT_FILE_NAME);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTasklet_NonexistentRegion_PartialSuccess() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-test-partial-success-non-existent-region.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("A Tribunal Hearing Centre")
                .courtName("A TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .build()
        ), 1);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "PartialSuccess", UPLOAD_COURT_FILE_NAME);
        Quartet<String, String, String, Long> quartet =
            with(REGION_ID, REGION_ID_NOT_EXISTS, "123456", 3L);
        validateLrdServiceFileJsrException(jdbcTemplate, orderedExceptionQuery, 3,
                                           COURT_VENUE_TABLE_NAME, quartet);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTasklet_NonexistentCluster_PartialSuccess() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-test-partial-success-non-existent-cluster.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("A Tribunal Hearing Centre")
                .courtName("A TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .build()
        ), 1);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "PartialSuccess", UPLOAD_COURT_FILE_NAME);
        Quartet<String, String, String, Long> quartet =
            with(CLUSTER_ID, CLUSTER_ID_NOT_EXISTS, "123456", 3L);
        validateLrdServiceFileJsrException(jdbcTemplate, orderedExceptionQuery, 3,
                                           COURT_VENUE_TABLE_NAME, quartet);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTasklet_NonexistentCourtTypeId_PartialSuccess() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-test-partial-success-non-existent-court-type-id.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("A Tribunal Hearing Centre")
                .courtName("A TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .build()
        ), 1);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "PartialSuccess", UPLOAD_COURT_FILE_NAME);
        Quartet<String, String, String, Long> quartet =
            with(COURT_TYPE_ID, COURT_TYPE_ID_NOT_EXISTS, "123456", 3L);
        validateLrdServiceFileJsrException(jdbcTemplate, orderedExceptionQuery, 3,
                                           COURT_VENUE_TABLE_NAME, quartet);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTasklet_NonexistentEpimmsId_PartialSuccess() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-test-partial-success-non-existent-epimms-id.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("A Tribunal Hearing Centre")
                .courtName("A TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .build()
        ), 1);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "PartialSuccess", UPLOAD_COURT_FILE_NAME);
        Quartet<String, String, String, Long> quartet =
            with(EPIMMS_ID, EPIMMS_ID_NOT_EXISTS, "a123456", 3L);
        validateLrdServiceFileJsrException(jdbcTemplate, orderedExceptionQuery, 3,
                                           COURT_VENUE_TABLE_NAME, quartet);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTasklet_NonexistentServiceCode_PartialSuccess() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-test-partial-success-non-existent-service-code.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateLrdCourtVenueFile(jdbcTemplate, lrdCourtVenueSelectData, List.of(
            CourtVenue.builder().epimmsId("123456").siteName("A Tribunal Hearing Centre")
                .courtName("A TRIBUNAL HEARING CENTRE").courtStatus("Open").regionId("7").courtTypeId("17")
                .openForPublic("Yes").courtAddress("AB1,48 COURT STREET,LONDON").postcode("AB12 3AB")
                .phoneNumber("").closedDate(null).courtLocationCode("").dxAddress("").welshSiteName("")
                .welshCourtAddress("").venueName("").isCaseManagementLocation("").isHearingLocation("")
                .welshVenueName("testVenue1").isTemporaryLocation("N").isNightingaleCourt("N")
                .locationType("Court").parentLocation("366559").welshCourtName("testWelshCourtName")
                .uprn("uprn123").venueOuCode("venueOuCode1").mrdBuildingLocationId("mrdBId1")
                .mrdVenueId("mrdVenueId1").serviceUrl("serviceUrl1").factUrl("factUrl1")
                .mrdCreatedTime("2022-04-01 02:00:01").mrdUpdatedTime("2022-04-01 02:00:02")
                .mrdDeletedTime("2022-04-01 02:00:03").externalShortName("External Short Name")
                .welshExternalShortName("Welsh External Short Name")
                .serviceCode("AAA1").build()
        ), 1);
        //Validates Success Audit
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "PartialSuccess", UPLOAD_COURT_FILE_NAME);
        Quartet<String, String, String, Long> quartet =
            with(SERVICE_CODE, SERVICE_CODE_NOT_EXISTS, "123456", 3L);
        validateLrdServiceFileJsrException(jdbcTemplate, orderedExceptionQuery, 3,
                                           COURT_VENUE_TABLE_NAME, quartet);
    }

    @Test
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testTaskletFailure() throws Exception {
        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-failure.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var courtVenues = jdbcTemplate.queryForList(lrdCourtVenueSelectData);
        assertThat(courtVenues).hasSize(0);

        Pair<String, String> pair = new Pair<>(
            UPLOAD_COURT_FILE_NAME,
            "Court Venue upload failed as no valid records present"
        );
        validateLrdServiceFileException(jdbcTemplate, exceptionQuery, pair, 5);
        validateLrdServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_COURT_FILE_NAME);
    }

    @Test
    @DisplayName("Status: PartialSucess - Test for 0 byte characters.")
    @Sql(scripts = {"/testData/truncate-lrd-court-venue.sql", "/testData/insert-building-location.sql"})
    void testFlagServiceCsv_0_byte_character() throws Exception {

        lrdBlobSupport.uploadFile(
            UPLOAD_COURT_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/courtVenues/court-venue-0-byte-character.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var flagServiceValues = jdbcTemplate.queryForList(lrdCourtVenueSelectData);
        assertEquals(2, flagServiceValues.size());

        String zer0ByteCharacterErrorMsg = "Zero byte characters identified - check source file";

        var result = jdbcTemplate.queryForList(exceptionQuery);
        MatcherAssert.assertThat(
            (String) result.get(3).get("error_description"),
            containsString(zer0ByteCharacterErrorMsg)
        );
        var auditResult = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(3, auditResult.size());
        Optional<Map<String, Object>> auditEntry =
            auditResult.stream().filter(audit -> audit.containsValue(UPLOAD_COURT_FILE_NAME)).findFirst();
        assertTrue(auditEntry.isPresent());
        auditEntry.ifPresent(audit -> assertEquals("Failure", audit.get("status")));

    }

    @AfterEach
    void tearDown() throws Exception {
        //Delete Uploaded test file with Snapshot delete
        lrdBlobSupport.deleteBlob(UPLOAD_COURT_FILE_NAME);
    }

    private void validateChildTables(int expectedSize) {
        assertThat(jdbcTemplate.queryForList(COURT_VENUE_LOCATION_DETAILS_SQL)).hasSize(expectedSize);
        assertThat(jdbcTemplate.queryForList(COURT_VENUE_REFERENCE_DETAILS_SQL)).hasSize(expectedSize);
    }

    private void assertReferenceValue(String courtTypeId, String columnName, String expectedValue) {
        Optional<Map<String, Object>> record = jdbcTemplate.queryForList(COURT_VENUE_REFERENCE_DETAILS_SQL).stream()
            .filter(row -> courtTypeId.equals(row.get("court_type_id")))
            .findFirst();
        assertTrue(record.isPresent());
        record.ifPresent(row -> assertEquals(expectedValue, row.get(columnName)));
    }

    private void assertLocationValue(String courtTypeId, String columnName, String expectedValue) {
        Optional<Map<String, Object>> record = jdbcTemplate.queryForList(COURT_VENUE_LOCATION_DETAILS_SQL).stream()
            .filter(row -> courtTypeId.equals(row.get("court_type_id")))
            .findFirst();
        assertTrue(record.isPresent());
        record.ifPresent(row -> assertEquals(expectedValue, row.get(columnName)));
    }
}
