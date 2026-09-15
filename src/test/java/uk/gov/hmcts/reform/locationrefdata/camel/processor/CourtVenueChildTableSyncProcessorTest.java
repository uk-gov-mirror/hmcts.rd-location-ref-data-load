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
import uk.gov.hmcts.reform.locationrefdata.camel.binder.CourtVenue;
import uk.gov.hmcts.reform.locationrefdata.camel.service.ChildTableDataSyncService;
import uk.gov.hmcts.reform.locationrefdata.camel.service.ChildTableSyncDefinition;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
            .singleElement()
            .satisfies(row -> assertThat(row).containsEntry("contact_method_code", "PHONE")
                .containsEntry("language_code", "EN")
                .containsEntry("contact_method_desc", "Phone"));
        assertThat(rowsCaptor.getAllValues().get(3))
            .hasSize(8)
            .anySatisfy(row -> assertThat(row).containsEntry("court_name_type", "SITE")
                .containsEntry("language_code", "EN")
                .containsEntry("name_desc", "Site name"));
        assertThat(rowsCaptor.getAllValues().get(4))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("address_type", "MAILING")
                .containsEntry("language_code", "EN")
                .containsEntry("address", "Court address"));
        assertThat(rowsCaptor.getAllValues().get(5))
            .singleElement()
            .satisfies(row -> assertThat(row).containsEntry("contact_method_code", "PHONE")
                .containsEntry("contact_type_code", "CONTACT_SERVICE")
                .containsEntry("contact_value", "01234567890"));
        assertThat(rowsCaptor.getAllValues().get(6))
            .hasSize(2)
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "CASE_MANAGEMENT"))
            .anySatisfy(row -> assertThat(row).containsEntry("use_type_code", "TEMPORARY"));
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
            .courtAddress("Court address")
            .welshCourtAddress("Welsh court address")
            .postcode("SW1A 1AA")
            .uprn("123")
            .phoneNumber("01234567890")
            .isCaseManagementLocation("Y")
            .isHearingLocation("N")
            .isTemporaryLocation("y")
            .isNightingaleCourt("N")
            .courtLocationCode("ABC")
            .venueOuCode("DEF")
            .serviceUrl("https://service.example")
            .factUrl("https://fact.example")
            .build();
    }
}
