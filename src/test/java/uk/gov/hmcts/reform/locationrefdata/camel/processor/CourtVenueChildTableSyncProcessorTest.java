package uk.gov.hmcts.reform.locationrefdata.camel.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CourtVenueChildTableSyncProcessorTest {

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
        when(jdbcTemplate.queryForList("SELECT mrd_venue_id FROM court_venue")).thenReturn(List.of());

        processor.process(exchange);

        ArgumentCaptor<ChildTableSyncDefinition> definitionCaptor =
            ArgumentCaptor.forClass(ChildTableSyncDefinition.class);
        ArgumentCaptor<List<Map<String, Object>>> rowsCaptor = ArgumentCaptor.forClass(List.class);

        verify(childTableDataSyncService, times(9))
            .sync(definitionCaptor.capture(), rowsCaptor.capture());
        verify(jdbcTemplate).update(
            "UPDATE court_venue SET court_status_code = NULL WHERE court_status_code IS NOT NULL"
        );
        ArgumentCaptor<List<Object[]>> statusUpdateCaptor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(
            eq("UPDATE court_venue SET court_status_code = ? WHERE mrd_venue_id = ?"),
            statusUpdateCaptor.capture()
        );
        assertThat(statusUpdateCaptor.getValue())
            .singleElement()
            .satisfies(values -> assertThat(values).containsExactly("OPEN", "venue-1"));

        assertThat(definitionCaptor.getAllValues())
            .extracting(ChildTableSyncDefinition::tableName)
            .containsExactly(
                "contact_details",
                "court_status",
                "contact_method",
                "court_venue_name",
                "address",
                "contact_details",
                "court_use_mapping",
                "reference_codes",
                "court_venue_url"
            );

        assertThat(rowsCaptor.getAllValues().get(0))
            .isEmpty();
        assertThat(rowsCaptor.getAllValues().get(1))
            .singleElement()
            .satisfies(row -> assertThat(row).containsEntry("court_status_code", "OPEN")
                .containsEntry("language_code", "EN")
                .containsEntry("court_status_desc", "Open"));
        assertThat(rowsCaptor.getAllValues().get(2))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("contact_method_code", "PHONE")
                .containsEntry("language_code", "EN")
                .containsEntry("contact_method_desc", "Phone"))
            .anySatisfy(row -> assertThat(row).containsEntry("contact_method_code", "EMAIL")
                .containsEntry("language_code", "EN")
                .containsEntry("contact_method_desc", "Email"));
        assertThat(rowsCaptor.getAllValues().get(3))
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
        assertThat(rowsCaptor.getAllValues().get(4))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("address_type", "MAILING")
                .containsEntry("language_code", "EN")
                .containsEntry("address", "Court address"));
        assertThat(rowsCaptor.getAllValues().get(5))
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
        assertThat(rowsCaptor.getAllValues().get(6))
            .hasSize(4)
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "CASE_MANAGEMENT"))
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "TEMPORARY"))
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "DISTRICT_REGISTRY"))
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "APPEAL_CENTRE"));
        assertThat(rowsCaptor.getAllValues().get(7))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("reference_code_type", "COURT_LOCATION_CODE"))
            .anySatisfy(row -> assertThat(row).containsEntry("reference_code_type", "VENUE_OU_CODE"));
        assertThat(rowsCaptor.getAllValues().get(8))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("url_type", "SERVICE"))
            .anySatisfy(row -> assertThat(row).containsEntry("url_type", "FACT"));
    }

    @Test
    void processDefersSyncUntilTransactionCommits() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(courtVenue()));
        when(jdbcTemplate.queryForList("SELECT mrd_venue_id FROM court_venue")).thenReturn(List.of());
        TransactionSynchronizationManager.initSynchronization();

        try {
            processor.process(exchange);

            verify(childTableDataSyncService, never()).sync(
                any(ChildTableSyncDefinition.class),
                anyList()
            );
            verify(jdbcTemplate, never()).update(anyString());

            TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

            verify(childTableDataSyncService, times(9)).sync(
                any(ChildTableSyncDefinition.class),
                anyList()
            );
            verify(jdbcTemplate).update(anyString());
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
            anyList()
        );
    }

    @Test
    void processUsesStoredCourtVenuesWhenExchangePropertyIsNotAvailable() {
        new CourtVenueFileDataStore().addAll("default", List.of(courtVenue()));
        when(jdbcTemplate.queryForList("SELECT mrd_venue_id FROM court_venue")).thenReturn(List.of());

        processor.process(exchange);

        verify(childTableDataSyncService, times(9)).sync(
            any(ChildTableSyncDefinition.class),
            anyList()
        );
    }

    @Test
    void processDeletesCourtVenuesMissingFromFileAfterChildTables() {
        exchange.setProperty(CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY, List.of(courtVenue()));
        when(jdbcTemplate.queryForList("SELECT mrd_venue_id FROM court_venue")).thenReturn(List.of(
            Map.of("mrd_venue_id", "venue-1"),
            Map.of("mrd_venue_id", "venue-2")
        ));

        processor.process(exchange);

        ArgumentCaptor<List<Object[]>> venueIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(
            eq("UPDATE court_venue SET parent_id = NULL WHERE parent_id = ?"),
            venueIdsCaptor.capture()
        );
        assertThat(venueIdsCaptor.getValue())
            .singleElement()
            .satisfies(values -> assertThat(values).containsExactly("venue-2"));
        verify(jdbcTemplate).batchUpdate(
            eq("DELETE FROM court_district_family_jurisdiction_assoc "
                   + "WHERE court_location_id = (SELECT court_venue_id FROM court_venue WHERE mrd_venue_id = ?)"),
            anyList()
        );
        verify(jdbcTemplate).batchUpdate(
            eq("DELETE FROM court_district_civil_jurisdiction_assoc "
                   + "WHERE court_location_id = (SELECT court_venue_id FROM court_venue WHERE mrd_venue_id = ?)"),
            anyList()
        );
        verify(jdbcTemplate).batchUpdate(
            eq("UPDATE court_venue SET district_registry_venue_id = NULL WHERE district_registry_venue_id = ?"),
            anyList()
        );
        verify(jdbcTemplate).batchUpdate(
            eq("UPDATE court_venue SET appeal_centre_venue_id = NULL WHERE appeal_centre_venue_id = ?"),
            anyList()
        );

        InOrder inOrder = inOrder(childTableDataSyncService, jdbcTemplate);
        inOrder.verify(childTableDataSyncService, times(9)).sync(
            any(ChildTableSyncDefinition.class),
            anyList()
        );
        inOrder.verify(jdbcTemplate).batchUpdate(
            eq("DELETE FROM court_venue WHERE mrd_venue_id = ?"),
            venueIdsCaptor.capture()
        );
        assertThat(venueIdsCaptor.getValue())
            .singleElement()
            .satisfies(values -> assertThat(values).containsExactly("venue-2"));
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
