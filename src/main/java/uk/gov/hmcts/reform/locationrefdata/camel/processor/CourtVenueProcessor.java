package uk.gov.hmcts.reform.locationrefdata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.CourtVenue;
import uk.gov.hmcts.reform.locationrefdata.camel.util.LogDto;
import uk.gov.hmcts.reform.locationrefdata.configuration.DataQualityCheckConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.CLUSTER_ID;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.CLUSTER_ID_NOT_EXISTS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.COURT_TYPE_ID;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.COURT_TYPE_ID_NOT_EXISTS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.EPIMMS_ID;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.EPIMMS_ID_NOT_EXISTS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.REGION_ID;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.REGION_ID_NOT_EXISTS;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.SERVICE_CODE;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.SERVICE_CODE_BLANK;
import static uk.gov.hmcts.reform.locationrefdata.camel.constants.LrdDataLoadConstants.SERVICE_CODE_NOT_EXISTS;
import static uk.gov.hmcts.reform.locationrefdata.camel.processor.CourtVenueChildTableSyncProcessor.COURT_VENUES_EXCHANGE_PROPERTY;
import static uk.gov.hmcts.reform.locationrefdata.camel.util.LrdLoadUtils.checkIfValueNotInListIfPresent;
import static uk.gov.hmcts.reform.locationrefdata.camel.util.LrdLoadUtils.trim;

@Slf4j
@Component
public class CourtVenueProcessor extends JsrValidationBaseProcessor<CourtVenue>
    implements IClusterRegionProcessor<CourtVenue> {

    @Autowired
    JsrValidatorInitializer<CourtVenue> courtVenueJsrValidatorInitializer;

    @Value("${logging-component-name}")
    private String logComponentName;

    @Value("${region-query}")
    private String regionQuery;

    @Value("${cluster-query}")
    private String clusterQuery;

    @Value("${epimms-id-query}")
    private String epimmsIdQuery;

    @Value("${court-type-id-query}")
    private String courtTypeIdQuery;

    @Value("${service-code-query}")
    private String serviceCodeQuery;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final String ZERO_BYTE_CHARACTER_ERROR_MESSAGE =
        "Zero byte characters identified - check source file";

    /**
     * A court venue must always be linked to a service, so rows delivered without a service id
     * (ServiceID column) are never loaded, they are reported as exceptions instead. This switch is
     * temporary: while it is on, such rows are only skipped and the file is still loaded, so the
     * source data can be corrected. Once it is switched off, a file holding rows without a service
     * id fails the load.
     */
    public static final String SKIP_BLANK_SERVICE_CODE_PROPERTY =
        "court-venue.skip-rows-with-blank-service-code";

    @Autowired
    private Environment environment;

    @Autowired
    DataQualityCheckConfiguration dataQualityCheckConfiguration;

    private CourtVenueFileDataStore courtVenueFileDataStore = new CourtVenueFileDataStore();


    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) throws Exception {

        var courtVenues = exchange.getIn().getBody() instanceof List
            ? (List<CourtVenue>) exchange.getIn().getBody()
            : singletonList((CourtVenue) exchange.getIn().getBody());

        log.info(" {} Court Venue Records count before Validation {}::",
                 logComponentName, courtVenues.size()
        );

        List<CourtVenue> filteredCourtVenues = validate(
            courtVenueJsrValidatorInitializer,
            courtVenues
        );

        var jsrValidatedCourtVenues = filteredCourtVenues.size();

        log.info(" {} Court Venue Records count after Validation {}::",
                 logComponentName, jsrValidatedCourtVenues
        );

        final boolean courtVenuesWithoutServiceCode =
            filterCourtVenuesWithBlankServiceCode(filteredCourtVenues, exchange);

        filterCourtVenuesForForeignKeyViolations(filteredCourtVenues, exchange);

        audit(courtVenueJsrValidatorInitializer, exchange);

        if (filteredCourtVenues.isEmpty()) {
            log.error(" {} Court Venue upload failed as no valid records present::", logComponentName);
            throw new RouteFailedException("Court Venue upload failed as no valid records present");
        }

        if (courtVenuesWithoutServiceCode && !isSkipBlankServiceCodeEnabled()) {
            setFileStatus(exchange, applicationContext, FAILURE);
        } else if (filteredCourtVenues.size() != jsrValidatedCourtVenues) {
            setFileStatus(exchange, applicationContext,PARTIAL_SUCCESS);
        }

        if (courtVenues != null && !courtVenues.isEmpty()) {
            processExceptionRecords(exchange, courtVenues);
        }

        exchange.setProperty(COURT_VENUES_EXCHANGE_PROPERTY, List.copyOf(filteredCourtVenues));
        exchange.getMessage().setBody(filteredCourtVenues);
        storeCourtVenuesForChildTableSync(exchange, filteredCourtVenues);

    }

    private void storeCourtVenuesForChildTableSync(Exchange exchange, List<CourtVenue> filteredCourtVenues) {
        String jobKey = courtVenueFileDataStore.jobKey(exchange);
        Integer splitIndex = exchange.getProperty(Exchange.SPLIT_INDEX, Integer.class);
        if (splitIndex == null || splitIndex == 0) {
            courtVenueFileDataStore.reset(jobKey);
        }
        courtVenueFileDataStore.addAll(jobKey, filteredCourtVenues);
    }


    private void processExceptionRecords(Exchange exchange,
                                         List<CourtVenue> courtVenuesList) {

        List<Pair<String, Long>> distinctZeroByteCharacterRecords = zerobyteCharacterCheck(courtVenuesList);
        if (!distinctZeroByteCharacterRecords.isEmpty()) {
            setFileStatus(exchange, applicationContext,FAILURE);
            courtVenueJsrValidatorInitializer.auditJsrExceptions(distinctZeroByteCharacterRecords,null,
                                                                  ZERO_BYTE_CHARACTER_ERROR_MESSAGE,exchange);
        }
    }

    private List<Pair<String, Long>>  zerobyteCharacterCheck(List<CourtVenue> courtVenuesList) {
        List<Pair<String, Long>> zeroByteCharacterRecords = new ArrayList<>();
        courtVenuesList.forEach(courtVenue -> dataQualityCheckConfiguration.getZeroByteCharacters()
            .forEach(zeroByteChar -> {
                if (courtVenue.toString().contains(zeroByteChar)) {
                    zeroByteCharacterRecords.add(Pair.of(
                        courtVenue.getEpimmsId() + "::" + courtVenue.getCourtTypeId(),
                        courtVenue.getRowId()
                    ));
                }
            }));
        return zeroByteCharacterRecords.stream().distinct().collect(Collectors.toList());
    }

    private boolean filterCourtVenuesWithBlankServiceCode(List<CourtVenue> validatedCourtVenues, Exchange exchange) {
        if (validatedCourtVenues.isEmpty()) {
            return false;
        }

        var recordsBefore = validatedCourtVenues.size();
        checkForeignKeyConstraint(
            validatedCourtVenues,
            location -> StringUtils.isBlank(trim(location.getServiceCode())),
            SERVICE_CODE, SERVICE_CODE_BLANK,
            new LogDto(
                "{}:: Number of valid court venues after removing the rows without a service id: {}",
                logComponentName
            ),
            exchange, courtVenueJsrValidatorInitializer
        );

        return validatedCourtVenues.size() != recordsBefore;
    }

    private boolean isSkipBlankServiceCodeEnabled() {
        return environment.getProperty(SKIP_BLANK_SERVICE_CODE_PROPERTY, Boolean.class, false);
    }

    @SuppressWarnings("unchecked")
    public void filterCourtVenuesForForeignKeyViolations(List<CourtVenue> validatedCourtVenues,
                                                          Exchange exchange) {
        if (isNotEmpty(validatedCourtVenues)) {
            var epimmsIdList = getIdList(jdbcTemplate, epimmsIdQuery);
            checkForeignKeyConstraint(
                validatedCourtVenues,
                location -> checkIfValueNotInListIfPresent(location.getEpimmsId(), epimmsIdList),
                EPIMMS_ID, EPIMMS_ID_NOT_EXISTS,
                new LogDto(
                    "{}:: Number of valid court venues after applying the epimms Id check filter: {}",
                    logComponentName
                ),
                exchange, courtVenueJsrValidatorInitializer
            );
            if (isNotEmpty(validatedCourtVenues)) {
                var serviceCodeList = getIdList(jdbcTemplate, serviceCodeQuery);
                checkForeignKeyConstraint(
                    validatedCourtVenues,
                    location -> checkIfValueNotInListIfPresent(location.getServiceCode(), serviceCodeList),
                    SERVICE_CODE, SERVICE_CODE_NOT_EXISTS,
                    new LogDto(
                        "{}:: Number of valid court venues after applying the service code check filter: {}",
                        logComponentName
                    ),
                    exchange, courtVenueJsrValidatorInitializer
                );
                if (isNotEmpty(validatedCourtVenues)) {
                    var courtTypeIdList = getIdList(jdbcTemplate, courtTypeIdQuery);
                    checkForeignKeyConstraint(
                        validatedCourtVenues,
                        location -> checkIfValueNotInListIfPresent(location.getCourtTypeId(),
                                                               courtTypeIdList),
                        COURT_TYPE_ID, COURT_TYPE_ID_NOT_EXISTS, new LogDto(
                            "{}:: Number of valid court venues after applying the court type "
                                + "id check filter: {}",logComponentName),exchange, courtVenueJsrValidatorInitializer
                    );
                    if (isNotEmpty(validatedCourtVenues)) {
                        var regionIdList = getIdList(jdbcTemplate, regionQuery);
                        checkForeignKeyConstraint(validatedCourtVenues,
                            location -> checkIfValueNotInListIfPresent(location.getRegionId(),regionIdList),
                                                  REGION_ID, REGION_ID_NOT_EXISTS,
                            new LogDto(
                            "{}:: Number of valid court venues after applying the region check filter: {}",
                            logComponentName
                        ),
                            exchange, courtVenueJsrValidatorInitializer
                        );
                        if (isNotEmpty(validatedCourtVenues)) {
                            var clusterIdList = getIdList(jdbcTemplate, clusterQuery);
                            checkForeignKeyConstraint(
                                validatedCourtVenues,location -> checkIfValueNotInListIfPresent(
                                    location.getClusterId(), clusterIdList),CLUSTER_ID, CLUSTER_ID_NOT_EXISTS,
                                new LogDto(
                                "{}:: Number of valid court venues after applying the cluster check filter: {}",
                                logComponentName
                                    ),
                                exchange, courtVenueJsrValidatorInitializer
                            );
                        }
                    }
                }
            }
        }
    }
}
