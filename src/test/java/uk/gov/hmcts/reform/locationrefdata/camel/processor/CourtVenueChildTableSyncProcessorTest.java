package uk.gov.hmcts.reform.locationrefdata.camel.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.CourtVenue;
import uk.gov.hmcts.reform.locationrefdata.camel.service.ChildTableDataSyncService;
import uk.gov.hmcts.reform.locationrefdata.camel.service.ChildTableSyncDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CourtVenueChildTableSyncProcessorTest {

    private static final int SYNCED_CHILD_TABLES = 8;

    @Mock
    private ChildTableDataSyncService childTableDataSyncService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private CourtVenueChildTableSyncProcessor processor;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        new CourtVenueFileDataStore().reset("default");
        processor = new CourtVenueChildTableSyncProcessor(childTableDataSyncService, jdbcTemplate);
        CamelContext camelContext = new DefaultCamelContext();
        exchange = new DefaultExchange(camelContext);
    }

    @Test
    void processSyncsAllCourtVenueChildTables() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(courtVenue()));

        processor.process(exchange);

        ArgumentCaptor<ChildTableSyncDefinition> definitionCaptor =
            ArgumentCaptor.forClass(ChildTableSyncDefinition.class);
        verify(childTableDataSyncService, times(SYNCED_CHILD_TABLES))
            .sync(definitionCaptor.capture(), anyList(), anySet());
        assertThat(definitionCaptor.getAllValues())
            .extracting(ChildTableSyncDefinition::tableName)
            .containsExactly(
                "court_status",
                "contact_method",
                "court_venue_name",
                "address",
                "contact_details",
                "court_use_mapping",
                "reference_codes",
                "court_venue_url"
            );

        ArgumentCaptor<List<Object[]>> statusClearCaptor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(
            eq("UPDATE court_venue SET court_status_code = NULL WHERE court_status_code IS NOT NULL "
                   + "AND mrd_venue_id = ?"),
            statusClearCaptor.capture()
        );
        assertThat(statusClearCaptor.getValue())
            .singleElement()
            .satisfies(values -> assertThat(values).containsExactly("venue-1"));

        ArgumentCaptor<List<Object[]>> statusUpdateCaptor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(
            eq("UPDATE court_venue SET court_status_code = ? WHERE mrd_venue_id = ?"),
            statusUpdateCaptor.capture()
        );
        assertThat(statusUpdateCaptor.getValue())
            .singleElement()
            .satisfies(values -> assertThat(values).containsExactly("OPEN", "venue-1"));

        assertThat(rowsFor("court_status"))
            .singleElement()
            .satisfies(row -> assertThat(row).containsEntry("court_status_code", "OPEN")
                .containsEntry("language_code", "EN")
                .containsEntry("court_status_desc", "Open"));
        assertThat(rowsFor("contact_method"))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("contact_method_code", "PHONE")
                .containsEntry("language_code", "EN")
                .containsEntry("contact_method_desc", "Phone"))
            .anySatisfy(row -> assertThat(row).containsEntry("contact_method_code", "EMAIL")
                .containsEntry("language_code", "EN")
                .containsEntry("contact_method_desc", "Email"));
        assertThat(rowsFor("court_venue_name"))
            .hasSize(12)
            .anySatisfy(row -> assertThat(row).containsEntry("court_name_type", "SITE")
                .containsEntry("language_code", "EN")
                .containsEntry("name_desc", "Site name"))
            .anySatisfy(row -> assertThat(row).containsEntry("court_name_type", "DISTRICT_REGISTRY_SITE")
                .containsEntry("language_code", "EN")
                .containsEntry("name_desc", "District registry site"))
            .anySatisfy(row -> assertThat(row).containsEntry("court_name_type", "DISTRICT_REGISTRY_EXTERNAL_SHORT")
                .containsEntry("language_code", "CY")
                .containsEntry("name_desc", "Welsh district registry external short"));
        assertThat(rowsFor("address"))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("address_type", "MAILING")
                .containsEntry("language_code", "EN")
                .containsEntry("address", "Court address"));
        assertThat(rowsFor("contact_details"))
            .hasSize(3)
            .anySatisfy(row -> assertThat(row).containsEntry("contact_method_code", "PHONE")
                .containsEntry("contact_type_code", "CONTACT_SERVICE")
                .containsEntry("contact_value", "01234567890"))
            .anySatisfy(row -> assertThat(row).containsEntry("contact_method_code", "EMAIL")
                .containsEntry("contact_type_code", "CONTACT_SERVICE")
                .containsEntry("contact_value", "contact@example.com"))
            .anySatisfy(row -> assertThat(row).containsEntry("contact_method_code", "EMAIL")
                .containsEntry("contact_type_code", "BREATHING_SPACE")
                .containsEntry("contact_value", "breathing@example.com"));
        assertThat(rowsFor("court_use_mapping"))
            .hasSize(4)
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "CASE_MANAGEMENT"))
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "TEMPORARY"))
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "DISTRICT_REGISTRY"))
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "APPEAL_CENTRE"));
        assertThat(rowsFor("reference_codes"))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("reference_code_type", "COURT_LOCATION_CODE"))
            .anySatisfy(row -> assertThat(row).containsEntry("reference_code_type", "VENUE_OU_CODE"));
        assertThat(rowsFor("court_venue_url"))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("url_type", "SERVICE"))
            .anySatisfy(row -> assertThat(row).containsEntry("url_type", "FACT"));
    }

    @Test
    void processScopesChildTableDeletesToTheVenueIdsInTheFile() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(courtVenue()));

        processor.process(exchange);

        ArgumentCaptor<Set<String>> scopeCaptor = ArgumentCaptor.forClass(Set.class);
        verify(childTableDataSyncService, times(SYNCED_CHILD_TABLES))
            .sync(any(ChildTableSyncDefinition.class), anyList(), scopeCaptor.capture());
        assertThat(scopeCaptor.getAllValues())
            .allSatisfy(scope -> assertThat(scope).containsExactly("venue-1"));
    }

    @Test
    void processDoesNotDeleteCourtVenuesMissingFromTheFile() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(courtVenue()));

        processor.process(exchange);

        verify(jdbcTemplate, never()).batchUpdate(eq("DELETE FROM court_venue WHERE mrd_venue_id = ?"), anyList());
        verify(jdbcTemplate, never()).batchUpdate(
            eq("DELETE FROM court_district_family_jurisdiction_assoc "
                   + "WHERE court_location_id = (SELECT court_venue_id FROM court_venue WHERE mrd_venue_id = ?)"),
            anyList()
        );
        verify(jdbcTemplate, never()).batchUpdate(
            eq("DELETE FROM court_district_civil_jurisdiction_assoc "
                   + "WHERE court_location_id = (SELECT court_venue_id FROM court_venue WHERE mrd_venue_id = ?)"),
            anyList()
        );
        verify(jdbcTemplate, never()).batchUpdate(
            eq("UPDATE court_venue SET district_registry_venue_id = NULL WHERE district_registry_venue_id = ?"),
            anyList()
        );
        verify(jdbcTemplate, never()).batchUpdate(
            eq("UPDATE court_venue SET appeal_centre_venue_id = NULL WHERE appeal_centre_venue_id = ?"),
            anyList()
        );
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    void processDefersSyncUntilTransactionCommits() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(courtVenue()));
        TransactionSynchronizationManager.initSynchronization();

        try {
            processor.process(exchange);

            verify(childTableDataSyncService, never()).sync(
                any(ChildTableSyncDefinition.class),
                anyList(),
                anySet()
            );
            verify(jdbcTemplate, never()).update(anyString());

            TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

            verify(childTableDataSyncService, times(SYNCED_CHILD_TABLES)).sync(
                any(ChildTableSyncDefinition.class),
                anyList(),
                anySet()
            );
            verify(jdbcTemplate).batchUpdate(
                eq("UPDATE court_venue SET court_status_code = NULL WHERE court_status_code IS NOT NULL "
                       + "AND mrd_venue_id = ?"),
                anyList()
            );
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void processSkipsRowsWithoutMrdVenueId() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(
            CourtVenue.builder().mrdVenueId(" ").build()
        ));

        processor.process(exchange);

        verify(childTableDataSyncService, never()).sync(
            any(ChildTableSyncDefinition.class),
            anyList(),
            anySet()
        );
        verifyNoMoreInteractions(jdbcTemplate);
    }

    @Test
    void processUsesStoredCourtVenuesWhenExchangePropertyIsNotAvailable() {
        new CourtVenueFileDataStore().addAll("default", List.of(courtVenue()));

        processor.process(exchange);

        verify(childTableDataSyncService, times(SYNCED_CHILD_TABLES)).sync(
            any(ChildTableSyncDefinition.class),
            anyList(),
            anySet()
        );
    }

    @Test
    void processAddsEnglishAddressRowWithoutCourtAddressWhenOnlyPostcodeExists() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(
            CourtVenue.builder()
                .mrdVenueId("venue-1")
                .courtAddress("  ")
                .postcode("SW1A 1AA")
                .uprn("123")
                .build()
        ));

        processor.process(exchange);

        assertThat(rowsFor("address"))
            .singleElement()
            .satisfies(row -> assertThat(row)
                .containsEntry("mrd_venue_id", "venue-1")
                .containsEntry("address_type", "MAILING")
                .containsEntry("language_code", "EN")
                .containsEntry("address", "")
                .containsEntry("post_code", "SW1A 1AA")
                .containsEntry("uprn", "123"));
    }

    @Test
    void processAddsEnglishAddressRowWithCourtAddressWhenPostcodeIsMissing() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(
            CourtVenue.builder()
                .mrdVenueId("venue-1")
                .courtAddress("Court address")
                .postcode("  ")
                .uprn("123")
                .build()
        ));

        processor.process(exchange);

        assertThat(rowsFor("address"))
            .singleElement()
            .satisfies(row -> assertThat(row)
                .containsEntry("mrd_venue_id", "venue-1")
                .containsEntry("address_type", "MAILING")
                .containsEntry("language_code", "EN")
                .containsEntry("address", "Court address")
                .containsEntry("post_code", null)
                .containsEntry("uprn", "123"));
    }

    @Test
    void processAddsNoEnglishAddressRowWhenCourtAddressAndPostcodeAreMissing() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(
            CourtVenue.builder()
                .mrdVenueId("venue-1")
                .courtAddress("  ")
                .postcode("  ")
                .welshCourtAddress("Welsh court address")
                .uprn("123")
                .build()
        ));

        processor.process(exchange);

        assertThat(rowsFor("address"))
            .singleElement()
            .satisfies(row -> assertThat(row)
                .containsEntry("mrd_venue_id", "venue-1")
                .containsEntry("address_type", "MAILING")
                .containsEntry("language_code", "CY")
                .containsEntry("address", "Welsh court address"));
    }

    @Test
    void processAddsNoWelshAddressRowWhenWelshCourtAddressIsMissing() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(
            CourtVenue.builder()
                .mrdVenueId("venue-1")
                .courtAddress("Court address")
                .postcode("SW1A 1AA")
                .uprn("123")
                .build()
        ));

        processor.process(exchange);

        assertThat(rowsFor("address"))
            .singleElement()
            .satisfies(row -> assertThat(row)
                .containsEntry("language_code", "EN")
                .containsEntry("address", "Court address"));
    }

    private List<Map<String, Object>> rowsFor(String tableName) {
        ArgumentCaptor<ChildTableSyncDefinition> definitionCaptor =
            ArgumentCaptor.forClass(ChildTableSyncDefinition.class);
        ArgumentCaptor<List<Map<String, Object>>> rowsCaptor = ArgumentCaptor.forClass(List.class);

        verify(childTableDataSyncService, times(SYNCED_CHILD_TABLES)).sync(
            definitionCaptor.capture(),
            rowsCaptor.capture(),
            anySet()
        );

        List<ChildTableSyncDefinition> definitions = definitionCaptor.getAllValues();
        for (int index = 0; index < definitions.size(); index++) {
            if (tableName.equals(definitions.get(index).tableName())) {
                return rowsCaptor.getAllValues().get(index);
            }
        }
        throw new AssertionError(tableName + " child table was not synced");
    }

    private CourtVenue courtVenue() {
        return CourtVenue.builder()
            .mrdVenueId("venue-1")
            .courtStatus("Open")
            .siteName(" Site name ")
            .courtName("Court name")
            .venueName("Venue name")
            .welshSiteName("Welsh site")
            .welshCourtName("Welsh court")
            .welshVenueName("Welsh venue")
            .externalShortName("External short")
            .welshExternalShortName("Welsh external short")
            .districtRegistrySiteName("District registry site")
            .districtRegistryWelshSiteName("Welsh district registry site")
            .districtRegistryExternalShortName("District registry external short")
            .districtRegistryWelshExternalShortName("Welsh district registry external short")
            .courtAddress("Court address")
            .welshCourtAddress("Welsh court address")
            .postcode("SW1A 1AA")
            .uprn("123")
            .phoneNumber("01234567890")
            .contactEmail("contact@example.com")
            .breathingSpaceEmail("breathing@example.com")
            .isCaseManagementLocation("Y")
            .isHearingLocation("N")
            .isTemporaryLocation("y")
            .isNightingaleCourt("N")
            .isDistrictRegistry("Y")
            .isAppealCentre("Y")
            .courtLocationCode("ABC")
            .venueOuCode("DEF")
            .serviceUrl("https://service.example")
            .factUrl("https://fact.example")
            .build();
    }
}
