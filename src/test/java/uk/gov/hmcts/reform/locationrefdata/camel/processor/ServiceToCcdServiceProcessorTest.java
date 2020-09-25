package uk.gov.hmcts.reform.locationrefdata.camel.processor;

import com.google.common.collect.ImmutableList;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdService;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SuppressWarnings("unchecked")
public class ServiceToCcdServiceProcessorTest {


    ServiceToCcdServiceProcessor serviceToCcdServiceProcessor = spy(new ServiceToCcdServiceProcessor());

    CamelContext camelContext = new DefaultCamelContext();

    Exchange exchange = new DefaultExchange(camelContext);

    JsrValidatorInitializer<ServiceToCcdService> serviceToCcdServiceJsrValidatorInitializer
        = new JsrValidatorInitializer<>();

    @Before
    public void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        setField(serviceToCcdServiceJsrValidatorInitializer, "validator", validator);
        setField(serviceToCcdServiceProcessor, "serviceToCcdServiceJsrValidatorInitializer",
                 serviceToCcdServiceJsrValidatorInitializer
        );
        setField(serviceToCcdServiceProcessor, "logComponentName",
                 "testlogger"
        );
    }

    @Test
    public void testProcess() throws Exception {

        List<ServiceToCcdService> serviceToCcdServices = new ArrayList<>();
        serviceToCcdServices.add(ServiceToCcdService.builder().ccdServiceName("service1,service2")
                                     .ccdJurisdictionName("service1 Jurisdiction").serviceCode("1111").build());
        serviceToCcdServices.add(ServiceToCcdService.builder().ccdServiceName("service1")
                                     .ccdJurisdictionName("service1 Jurisdiction").serviceCode("1112").build());

        exchange.getIn().setBody(serviceToCcdServices);
        doNothing().when(serviceToCcdServiceProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        serviceToCcdServiceProcessor.process(exchange);
        verify(serviceToCcdServiceProcessor, times(1)).process(exchange);

        List<ServiceToCcdService> resultList = ((List<ServiceToCcdService>) exchange.getMessage().getBody());

        assertEquals(resultList.size(), 3);
        List<ServiceToCcdService> excepted = ImmutableList.of(
            ServiceToCcdService.builder().ccdServiceName("service1")
                .ccdJurisdictionName("service1 Jurisdiction").serviceCode("1111").build(),
            ServiceToCcdService.builder().ccdServiceName("service2")
                .ccdJurisdictionName("service1 Jurisdiction").serviceCode("1111").build(),
            ServiceToCcdService.builder().ccdServiceName("service1")
                .ccdJurisdictionName("service1 Jurisdiction").serviceCode("1112").build()
        );
        assertEquals(resultList, excepted);
    }

    @Test
    public void testProcessSingleElement() throws Exception {

        ServiceToCcdService serviceToCcdService = ServiceToCcdService.builder().ccdServiceName("service1,service2")
            .ccdJurisdictionName("service1 Jurisdiction").serviceCode("1111").build();

        exchange.getIn().setBody(serviceToCcdService);
        doNothing().when(serviceToCcdServiceProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        serviceToCcdServiceProcessor.process(exchange);
        verify(serviceToCcdServiceProcessor, times(1)).process(exchange);
        List<ServiceToCcdService> resultList = ((List<ServiceToCcdService>) exchange.getMessage().getBody());

        assertEquals(resultList.size(), 2);
        List<ServiceToCcdService> excepted = ImmutableList.of(
            ServiceToCcdService.builder().ccdServiceName("service1")
                .ccdJurisdictionName("service1 Jurisdiction")
                .serviceCode("1111").build(),
            ServiceToCcdService.builder().ccdServiceName("service2")
                .ccdJurisdictionName("service1 Jurisdiction")
                .serviceCode("1111").build()
        );
        assertEquals(resultList, excepted);
    }

    @Test
    public void testProcessSingleElementWithException() throws Exception {

        ServiceToCcdService serviceToCcdService = ServiceToCcdService.builder().ccdServiceName("")
            .ccdJurisdictionName("service1 Jurisdiction").serviceCode("1111").build();

        exchange.getIn().setBody(serviceToCcdService);
        doNothing().when(serviceToCcdServiceProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        assertThrows(RouteFailedException.class, () -> serviceToCcdServiceProcessor.process(exchange));
        verify(serviceToCcdServiceProcessor, times(1)).process(exchange);
    }
}
