package uk.gov.hmcts.reform.locationrefdata.camel.processor;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.CourtVenue;
import uk.gov.hmcts.reform.locationrefdata.configuration.DataQualityCheckConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.EPIMMS_ID_AND_SERVICE_CODE;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.EPIMMS_ID_AND_SERVICE_CODE_DUPLICATE;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CourtVenueProcessorTest {

    @Spy
    private CourtVenueProcessor processor = new CourtVenueProcessor();

    CamelContext camelContext = new DefaultCamelContext();

    Exchange exchange = new DefaultExchange(camelContext);

    @Spy
    JsrValidatorInitializer<CourtVenue> courtVenueJsrValidatorInitializer = new JsrValidatorInitializer<>();

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    PlatformTransactionManager platformTransactionManager;

    @Mock
    ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Mock
    ConfigurableApplicationContext applicationContext;

    @Mock
    DataQualityCheckConfiguration dataQualityCheckConfiguration = new DataQualityCheckConfiguration();

    private static final List<Pair<String, Long>> ZERO_BYTE_CHARACTER_RECORDS = List.of(
        Pair.of("123::123", null),
        Pair.of("2::2", null));
    private static final List<Pair<String, Long>> DUPLICATE_COMPOSITE_KEY_RECORDS = List.of(
        Pair.of("1::AAA1", 3L));

    private static final List<String> ZERO_BYTE_CHARACTERS = List.of("\u200B", " ");

    @BeforeEach
    public void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        setField(dataQualityCheckConfiguration, "zeroByteCharacters", ZERO_BYTE_CHARACTERS);
        setField(courtVenueJsrValidatorInitializer, "validator", validator);
        setField(courtVenueJsrValidatorInitializer, "camelContext", camelContext);
        setField(processor, "jdbcTemplate", jdbcTemplate);
        setField(courtVenueJsrValidatorInitializer, "jdbcTemplate", jdbcTemplate);
        setField(courtVenueJsrValidatorInitializer, "platformTransactionManager",
            platformTransactionManager
        );
        setField(processor, "courtVenueJsrValidatorInitializer", courtVenueJsrValidatorInitializer);
        setField(processor, "logComponentName", "testlogger");
        setField(processor, "dataQualityCheckConfiguration",
            dataQualityCheckConfiguration);
        setField(processor, "regionQuery", "ids");
        setField(processor, "clusterQuery", "ids");
        setField(processor, "epimmsIdQuery", "ids");
        setField(processor, "courtTypeIdQuery", "ids");
        setField(processor, "serviceCodeQuery", "serviceCodes");
        setField(processor, "applicationContext", applicationContext);
        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setFileName("test");
        exchange.getIn().setHeader(ROUTE_DETAILS, routeProperties);
    }




    @Test
    void testProcess() throws Exception {
        List<CourtVenue> expectedCourtVenues = getValidCourtVenues();

        exchange.getIn().setBody(expectedCourtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List<CourtVenue> actualCourtVenues = (List<CourtVenue>) exchange.getMessage().getBody();

        assertThat(actualCourtVenues)
            .hasSize(2)
            .hasSameElementsAs(expectedCourtVenues);
    }

    @Test
    void testProcessWithValidAndInvalidCourtVenues() throws Exception {
        List<CourtVenue> courtVenues = new ArrayList<>();
        courtVenues.addAll(getInvalidCourtVenues());
        courtVenues.addAll(getValidCourtVenues());

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List<CourtVenue> actualCourtVenues = (List<CourtVenue>) exchange.getMessage().getBody();

        assertThat(actualCourtVenues)
            .hasSize(2)
            .usingRecursiveComparison().isEqualTo(getValidCourtVenues());
    }

    @Test
    void testProcessWithValidAndInvalidCourtVenues_Invalid_Region_Cluster_EpimsId() throws Exception {
        List<CourtVenue> courtVenues = new ArrayList<>();
        courtVenues.add(
            CourtVenue.builder()
                .epimmsId("1")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("abc")
                .courtTypeId("2")
                .clusterId("3")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build()
        );
        courtVenues.addAll(getValidCourtVenues());

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        when((applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List<CourtVenue> actualCourtVenues = (List<CourtVenue>) exchange.getMessage().getBody();

        assertThat(actualCourtVenues)
            .hasSize(2)
            .hasSameElementsAs(getValidCourtVenues());

        // testProcessWithValidAndInvalidCourtVenues_InvalidClusterId
        List<CourtVenue> courtVenues1 = new ArrayList<>();
        courtVenues1.add(
            CourtVenue.builder()
                .epimmsId("1")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("123")
                .courtTypeId("2")
                .clusterId("abc")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build()
        );
        courtVenues1.addAll(getValidCourtVenues());

        exchange.getIn().setBody(courtVenues1);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        when((applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        processor.process(exchange);
        verify(processor, times(2)).process(exchange);

        List<CourtVenue> actualCourtVenues1 = (List<CourtVenue>) exchange.getMessage().getBody();

        assertThat(actualCourtVenues1)
            .hasSize(2)
            .hasSameElementsAs(getValidCourtVenues());

        // testProcessWithValidAndInvalidCourtVenues_InvalidEpimmsId()

        List<CourtVenue> courtVenues2 = new ArrayList<>();
        courtVenues2.add(
            CourtVenue.builder()
                .epimmsId("epims123")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("123")
                .courtTypeId("2")
                .clusterId("1")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build()
        );
        courtVenues2.addAll(getValidCourtVenues());

        exchange.getIn().setBody(courtVenues2);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        setJdbcTemplateResponse();
        when((applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        processor.process(exchange);
        verify(processor, times(3)).process(exchange);

        List<CourtVenue> actualCourtVenues2 = (List<CourtVenue>) exchange.getMessage().getBody();

        assertThat(actualCourtVenues2)
            .hasSize(2)
            .hasSameElementsAs(getValidCourtVenues());
    }

    @Test
    void testProcessWithValidAndInvalidCourtVenues_InvalidCourtTypeId() throws Exception {
        List<CourtVenue> courtVenues = new ArrayList<>();
        courtVenues.add(
            CourtVenue.builder()
                .epimmsId("1")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("123")
                .courtTypeId("200000000")
                .clusterId("1")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build()
        );
        courtVenues.addAll(getValidCourtVenues());

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        when((applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List<CourtVenue> actualCourtVenues = (List<CourtVenue>) exchange.getMessage().getBody();

        assertThat(actualCourtVenues)
            .hasSize(2)
            .hasSameElementsAs(getValidCourtVenues());
    }

    @Test
    void testProcessWithValidAndInvalidCourtVenues_InvalidServiceCode() throws Exception {
        List<CourtVenue> courtVenues = new ArrayList<>();
        courtVenues.add(
            CourtVenue.builder()
                .epimmsId("1")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("123")
                .courtTypeId("2")
                .clusterId("1")
                .serviceCode("AAA9")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build()
        );
        courtVenues.addAll(getValidCourtVenues());

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        when((applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);
        verify(jdbcTemplate, times(1)).queryForList("serviceCodes", String.class);

        List<CourtVenue> actualCourtVenues = (List<CourtVenue>) exchange.getMessage().getBody();

        assertThat(actualCourtVenues)
            .hasSize(2)
            .hasSameElementsAs(getValidCourtVenues());
    }

    @Test
    void testProcessWithDuplicateEpimmsIdAndServiceCode() throws Exception {
        CourtVenue firstCourtVenue = CourtVenue.builder()
            .epimmsId("1")
            .siteName("Test Site")
            .courtName("Test Court Name")
            .courtStatus("Open")
            .courtOpenDate("12/12/12")
            .regionId("1")
            .courtTypeId("2")
            .clusterId("3")
            .openForPublic("Yes")
            .courtAddress("Test Court Address")
            .postcode("ABC 123")
            .phoneNumber("12343434")
            .closedDate("12/03/21")
            .courtLocationCode("12AB")
            .dxAddress("Test Dx Address")
            .serviceCode("AAA1")
            .welshSiteName("Test Welsh Site Name")
            .welshCourtAddress("Test Welsh Court Address")
            .build();
        firstCourtVenue.setRowId(2L);

        CourtVenue duplicateCourtVenue = CourtVenue.builder()
            .epimmsId("1")
            .siteName("Test Site Duplicate")
            .courtName("Test Court Name Duplicate")
            .courtStatus("Open")
            .courtOpenDate("12/12/12")
            .regionId("1")
            .courtTypeId("2")
            .clusterId("3")
            .openForPublic("Yes")
            .courtAddress("Test Court Address Duplicate")
            .postcode("ABC 123")
            .phoneNumber("12343434")
            .closedDate("12/03/21")
            .courtLocationCode("12AB")
            .dxAddress("Test Dx Address")
            .serviceCode("AAA1")
            .welshSiteName("Test Welsh Site Name")
            .welshCourtAddress("Test Welsh Court Address")
            .build();
        duplicateCourtVenue.setRowId(3L);

        exchange.getIn().setBody(List.of(firstCourtVenue, duplicateCourtVenue));
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        when((applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);

        List<CourtVenue> actualCourtVenues = (List<CourtVenue>) exchange.getMessage().getBody();

        assertThat(actualCourtVenues)
            .hasSize(1)
            .containsExactly(firstCourtVenue);
        verify(courtVenueJsrValidatorInitializer, times(1))
            .auditJsrExceptions(eq(DUPLICATE_COMPOSITE_KEY_RECORDS),
                eq(EPIMMS_ID_AND_SERVICE_CODE),
                eq(EPIMMS_ID_AND_SERVICE_CODE_DUPLICATE),
                eq(exchange));
    }

    @Test
    void testProcessWithInvalidCourtVenues() throws Exception {
        List<CourtVenue> courtVenues = getInvalidCourtVenues();

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        assertThrows(RouteFailedException.class, () -> processor.process(exchange));

        verify(processor, times(1)).process(exchange);
    }

    @Test
    @DisplayName("Test for 0 byte characters in record")
    void testCourtVenueCsv_0byte_characters() throws Exception {
        List<CourtVenue> courtVenuesList = new ArrayList<CourtVenue>();
        courtVenuesList.addAll(getZeroDataCourtVenues());

        exchange.getIn().setBody(courtVenuesList);

        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);

        when(((ConfigurableApplicationContext)applicationContext).getBeanFactory())
            .thenReturn(configurableListableBeanFactory);
        when((processor).validate(courtVenueJsrValidatorInitializer,courtVenuesList))
            .thenReturn(courtVenuesList);
        doNothing().when(processor).filterCourtVenuesForForeignKeyViolations(courtVenuesList, exchange);
        when(dataQualityCheckConfiguration.getZeroByteCharacters()).thenReturn(ImmutableList.of("\u200B"," "));
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List<CourtVenue> actualCourtVenueList = (List<CourtVenue>) exchange.getMessage().getBody();

        Assertions.assertEquals(2, actualCourtVenueList.size());
        verify(courtVenueJsrValidatorInitializer, times(1))
            .auditJsrExceptions(eq(ZERO_BYTE_CHARACTER_RECORDS),
                eq(null),
                eq("Zero byte characters identified - check source file"),
                eq(exchange));

    }

    @Test
    void testProcessWithSingleInvalidCourtVenue_InvalidRegionId() throws Exception {
        List<CourtVenue> courtVenues = ImmutableList.of(
            CourtVenue.builder()
                .epimmsId("1")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("abc")
                .courtTypeId("2")
                .clusterId("3")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build());

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        assertThrows(RouteFailedException.class, () -> processor.process(exchange));

        verify(processor, times(1)).process(exchange);
    }

    @Test
    void testProcessWithSingleInvalidCourtVenue_InvalidClusterId() throws Exception {
        List<CourtVenue> courtVenues = ImmutableList.of(
            CourtVenue.builder()
                .epimmsId("1")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("1")
                .courtTypeId("2")
                .clusterId("abc")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build());

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        assertThrows(RouteFailedException.class, () -> processor.process(exchange));

        verify(processor, times(1)).process(exchange);
    }

    @Test
    void testProcessWithSingleInvalidCourtVenue_InvalidEpimmsId() throws Exception {
        List<CourtVenue> courtVenues = ImmutableList.of(
            CourtVenue.builder()
                .epimmsId("abc")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("1")
                .courtTypeId("2")
                .clusterId("1")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build());

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        assertThrows(RouteFailedException.class, () -> processor.process(exchange));

        verify(processor, times(1)).process(exchange);
    }

    @Test
    void testProcessWithSingleInvalidCourtVenue_InvalidCourtTypeId() throws Exception {
        List<CourtVenue> courtVenues = ImmutableList.of(
            CourtVenue.builder()
                .epimmsId("1")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("1")
                .courtTypeId("20000000")
                .clusterId("1")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build());

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        assertThrows(RouteFailedException.class, () -> processor.process(exchange));

        verify(processor, times(1)).process(exchange);
    }

    @Test
    void testProcessWithSingleInvalidCourtVenue_InvalidServiceCode() throws Exception {
        List<CourtVenue> courtVenues = ImmutableList.of(
            CourtVenue.builder()
                .epimmsId("1")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("1")
                .courtTypeId("2")
                .clusterId("1")
                .serviceCode("AAA9")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .siteName("test site")
                .build());

        exchange.getIn().setBody(courtVenues);
        doNothing().when(processor).audit(courtVenueJsrValidatorInitializer, exchange);
        setJdbcTemplateResponse();
        assertThrows(RouteFailedException.class, () -> processor.process(exchange));

        verify(processor, times(1)).process(exchange);
        verify(jdbcTemplate, times(1)).queryForList("serviceCodes", String.class);
    }

    private List<CourtVenue> getInvalidCourtVenues() {
        return ImmutableList.of(
            CourtVenue.builder()
                .epimmsId("@£$%128")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("1")
                .courtTypeId("2")
                .clusterId("3")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .build());
    }


    private List<CourtVenue> getValidCourtVenues() {
        return ImmutableList.of(
            CourtVenue.builder()
                .epimmsId("1")
                .siteName("Test Site")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("1")
                .courtTypeId("2")
                .clusterId("3")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx Address")
                .serviceCode("AAA1")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .build(),
            CourtVenue.builder()
                .epimmsId("2")
                .siteName("Test Site1")
                .courtName("Test Court Name1")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("1")
                .courtTypeId("2")
                .clusterId("3")
                .openForPublic("No")
                .courtAddress("Test Court Address1")
                .postcode("ABD 123")
                .phoneNumber("12343434")
                .closedDate("12/03/22")
                .courtLocationCode("12AC")
                .dxAddress("Test Dx Address1")
                .serviceCode("AAA2")
                .welshSiteName("Test Welsh Site Name1")
                .welshCourtAddress("Test Welsh Court Address1")
                .build()
        );
    }

    private List<CourtVenue> getZeroDataCourtVenues() {
        return ImmutableList.of(
            CourtVenue.builder()
                .epimmsId("123")
                .siteName("Test \u200BSite")
                .courtName("Test Court Name")
                .courtStatus("Open")
                .courtOpenDate("12/12/12")
                .regionId("1")
                .courtTypeId("123")
                .clusterId("123")
                .openForPublic("Yes")
                .courtAddress("Test Court Address")
                .postcode("ABC 123")
                .phoneNumber("12343434")
                .closedDate("12/03/21")
                .courtLocationCode("12AB")
                .dxAddress("Test Dx  Address")
                .welshSiteName("Test Welsh Site Name")
                .welshCourtAddress("Test Welsh Court Address")
                .build(),
            CourtVenue.builder()
                .epimmsId("2")
                .siteName("Test Site1")
                .courtName("Test \u200BCourt Name1")
                .courtStatus(" Open")
                .courtOpenDate("12/12/12")
                .regionId("1")
                .courtTypeId("2")
                .clusterId("3")
                .openForPublic("No")
                .courtAddress("Test Court Address1")
                .postcode("ABD 123")
                .phoneNumber("12343434")
                .closedDate("12/03/22")
                .courtLocationCode("12AC")
                .dxAddress("Test Dx Address1")
                .welshSiteName("Test\u200B Welsh Site Name1")
                .welshCourtAddress("Test Welsh Court Address1")
                .build()
        );
    }

    private void setJdbcTemplateResponse() {
        when(jdbcTemplate.queryForList("ids", String.class)).thenReturn(ImmutableList.of("1", "2", "3"));
        lenient().when(jdbcTemplate.queryForList("serviceCodes", String.class))
            .thenReturn(ImmutableList.of("AAA1", "AAA2"));
    }

}
