package uk.gov.hmcts.reform.locationrefdata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdCaseType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

@Slf4j
@Component
public class ServiceToCcdCaseTypeProcessor extends JsrValidationBaseProcessor<ServiceToCcdCaseType> {

    @Autowired
    JsrValidatorInitializer<ServiceToCcdCaseType> serviceToCcdServiceJsrValidatorInitializer;

    @Value("${logging-component-name}")
    private String logComponentName;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) throws Exception {

        List<ServiceToCcdCaseType> serviceToCcdCaseTypes;

        serviceToCcdCaseTypes = exchange.getIn().getBody() instanceof List
            ? (List<ServiceToCcdCaseType>) exchange.getIn().getBody()
            : singletonList((ServiceToCcdCaseType) exchange.getIn().getBody());

        log.info(" {} ServiceToCCDService Records count before Validation & before merging service names {}::",
                 logComponentName, serviceToCcdCaseTypes.size()
        );

        List<ServiceToCcdCaseType> refinedServiceToCcdCaseTypes = populateService(serviceToCcdCaseTypes);
        log.info(" {} ServiceToCCDService Records count before Validation & after merging service names {}::",
                 logComponentName, refinedServiceToCcdCaseTypes.size()
        );

        List<ServiceToCcdCaseType> filteredServiceToCcdCaseTypes = validate(
            serviceToCcdServiceJsrValidatorInitializer,
            refinedServiceToCcdCaseTypes
        );
        log.info(" {} ServiceToCCDService Records count after Validation & after merging service names {}::",
                 logComponentName, filteredServiceToCcdCaseTypes.size()
        );

        audit(serviceToCcdServiceJsrValidatorInitializer, exchange);

        if (filteredServiceToCcdCaseTypes.size() == 0) {
            log.error(" {} ServiceToCcdService failed as no valid records present::", logComponentName);

            throw new RouteFailedException("ServiceToCcdService failed as no valid records present");
        }
        exchange.getMessage().setBody(filteredServiceToCcdCaseTypes);
    }

    /**
     * Spits list by , serviceNames and recreates the list.
     *
     * @param serviceToCcdCaseTypes serviceToCcdServices
     * @return List
     */
    private List<ServiceToCcdCaseType> populateService(List<ServiceToCcdCaseType> serviceToCcdCaseTypes) {

        Predicate<ServiceToCcdCaseType> isValidCaseType = e -> isNotEmpty(e.getCcdCaseType());
        Predicate<ServiceToCcdCaseType> isValidService = e -> isNotEmpty(e.getCcdServiceName())
            && isEmpty(e.getCcdCaseType());

        List<ServiceToCcdCaseType> refinedServiceToCcdCaseTypes = new ArrayList<>();
        serviceToCcdCaseTypes.stream()
            .filter(isValidCaseType)
            .forEach(serviceToCcdService ->
                         of(serviceToCcdService.getCcdCaseType().split(","))
                             .forEach(caseTypes -> {
                                 refinedServiceToCcdCaseTypes.add(ServiceToCcdCaseType.builder()
                                                                      .serviceCode(serviceToCcdService
                                                                                       .getServiceCode())
                                                                      .ccdServiceName(
                                                                          serviceToCcdService
                                                                              .getCcdServiceName())
                                                                      .ccdCaseType(caseTypes)
                                                                      .build());
                             }));

        refinedServiceToCcdCaseTypes.addAll(serviceToCcdCaseTypes.stream()
                                                .filter(isValidService).collect(toList()));

        return refinedServiceToCcdCaseTypes;
    }
}
