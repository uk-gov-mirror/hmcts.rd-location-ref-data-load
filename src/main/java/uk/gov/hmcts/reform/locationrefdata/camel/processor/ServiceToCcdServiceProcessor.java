package uk.gov.hmcts.reform.locationrefdata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Stream.of;

@Slf4j
@Component
public class ServiceToCcdServiceProcessor extends JsrValidationBaseProcessor<ServiceToCcdService> {

    @Autowired
    JsrValidatorInitializer<ServiceToCcdService> serviceToCcdServiceJsrValidatorInitializer;

    @Value("${logging-component-name}")
    private String logComponentName;

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) throws Exception {

        List<ServiceToCcdService> serviceToCcdServices;

        serviceToCcdServices = exchange.getIn().getBody() instanceof List
            ? (List<ServiceToCcdService>) exchange.getIn().getBody()
            : singletonList((ServiceToCcdService) exchange.getIn().getBody());

        log.info(" {} ServiceToCCDService Records count before Validation & before merging service names {}::",
                 logComponentName, serviceToCcdServices.size()
        );

        List<ServiceToCcdService> refinedServiceToCcdServices = populateService(serviceToCcdServices);
        log.info(" {} ServiceToCCDService Records count before Validation & after merging service names {}::",
                 logComponentName, refinedServiceToCcdServices.size()
        );

        List<ServiceToCcdService> filteredServiceToCcdServices = validate(
            serviceToCcdServiceJsrValidatorInitializer,
            refinedServiceToCcdServices
        );
        log.info(" {} ServiceToCCDService Records count after Validation & after merging service names {}::",
                 logComponentName, filteredServiceToCcdServices.size()
        );

        audit(serviceToCcdServiceJsrValidatorInitializer, exchange);

        if (filteredServiceToCcdServices.size() == 0) {
            log.error(" {} ServiceToCcdService failed as no valid records present::", logComponentName);

            throw new RouteFailedException("ServiceToCcdService failed as no valid records present");
        }
        exchange.getMessage().setBody(filteredServiceToCcdServices);
    }

    /**
     * Spits list by , serviceNames and recreates the list.
     *
     * @param serviceToCcdServices serviceToCcdServices
     * @return List
     */
    private List<ServiceToCcdService> populateService(List<ServiceToCcdService> serviceToCcdServices) {
        List<ServiceToCcdService> refinedServiceToCcdServices = new ArrayList<>();
        serviceToCcdServices.forEach(serviceToCcdService ->
                                         of(serviceToCcdService.getCcdServiceName().split(","))
                                             .forEach(serviceNames -> {
                                                 refinedServiceToCcdServices.add(ServiceToCcdService.builder()
                                                                                     .serviceCode(serviceToCcdService
                                                                                                      .getServiceCode())
                                                                                     .ccdJurisdictionName(
                                                                                         serviceToCcdService
                                                                                             .getCcdJurisdictionName())
                                                                                     .ccdServiceName(serviceNames)
                                                                                     .build());
                                             }));
        return refinedServiceToCcdServices;
    }
}
