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
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdCaseType;

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
public class ServiceToCcdCaseTypeProcessorTest {


    ServiceToCcdCaseTypeProcessor serviceToCcdCaseTypeProcessor = spy(new ServiceToCcdCaseTypeProcessor());

    CamelContext camelContext = new DefaultCamelContext();

    Exchange exchange = new DefaultExchange(camelContext);

    JsrValidatorInitializer<ServiceToCcdCaseType> serviceToCcdServiceJsrValidatorInitializer
        = new JsrValidatorInitializer<>();

    @Before
    public void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        setField(serviceToCcdServiceJsrValidatorInitializer, "validator", validator);
        setField(serviceToCcdCaseTypeProcessor, "serviceToCcdServiceJsrValidatorInitializer",
                 serviceToCcdServiceJsrValidatorInitializer
        );
        setField(serviceToCcdCaseTypeProcessor, "logComponentName",
                 "testlogger"
        );
    }

    @Test
    public void testProcess() throws Exception {

        List<ServiceToCcdCaseType> serviceToCcdCaseTypes = new ArrayList<>();
        serviceToCcdCaseTypes.add(ServiceToCcdCaseType.builder().ccdCaseType("service1,service2")
                                      .ccdServiceName("service1 Jurisdiction").serviceCode("1111").build());
        serviceToCcdCaseTypes.add(ServiceToCcdCaseType.builder().ccdCaseType("service1")
                                      .ccdServiceName("service1 Jurisdiction").serviceCode("1112").build());

        exchange.getIn().setBody(serviceToCcdCaseTypes);
        doNothing().when(serviceToCcdCaseTypeProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        serviceToCcdCaseTypeProcessor.process(exchange);
        verify(serviceToCcdCaseTypeProcessor, times(1)).process(exchange);

        List<ServiceToCcdCaseType> resultList = ((List<ServiceToCcdCaseType>) exchange.getMessage().getBody());

        assertEquals(3, resultList.size());
        List<ServiceToCcdCaseType> excepted = ImmutableList.of(
            ServiceToCcdCaseType.builder().ccdCaseType("service1")
                .ccdServiceName("service1 Jurisdiction").serviceCode("1111").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service2")
                .ccdServiceName("service1 Jurisdiction").serviceCode("1111").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service1")
                .ccdServiceName("service1 Jurisdiction").serviceCode("1112").build()
        );
        assertEquals(resultList, excepted);
    }

    @Test
    public void testProcessSingleElement() throws Exception {

        ServiceToCcdCaseType serviceToCcdCaseType = ServiceToCcdCaseType.builder().ccdCaseType("service1,service2")
            .ccdServiceName("service1 Jurisdiction").serviceCode("1111").build();

        exchange.getIn().setBody(serviceToCcdCaseType);
        doNothing().when(serviceToCcdCaseTypeProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        serviceToCcdCaseTypeProcessor.process(exchange);
        verify(serviceToCcdCaseTypeProcessor, times(1)).process(exchange);
        List<ServiceToCcdCaseType> resultList = ((List<ServiceToCcdCaseType>) exchange.getMessage().getBody());

        assertEquals(2, resultList.size());
        List<ServiceToCcdCaseType> excepted = ImmutableList.of(
            ServiceToCcdCaseType.builder().ccdCaseType("service1")
                .ccdServiceName("service1 Jurisdiction")
                .serviceCode("1111").build(),
            ServiceToCcdCaseType.builder().ccdCaseType("service2")
                .ccdServiceName("service1 Jurisdiction")
                .serviceCode("1111").build()
        );
        assertEquals(resultList, excepted);
    }

    @Test
    public void testProcessOneValidAndOneInvalidServiceCode() throws Exception {
        List<ServiceToCcdCaseType> serviceToCcdCaseTypes = new ArrayList<>();
        serviceToCcdCaseTypes.add(ServiceToCcdCaseType.builder().serviceCode("1111").build());
        serviceToCcdCaseTypes.add(ServiceToCcdCaseType.builder().ccdCaseType("service1")
                                      .ccdServiceName("service1 Jurisdiction").serviceCode("1112").build());

        exchange.getIn().setBody(serviceToCcdCaseTypes);
        doNothing().when(serviceToCcdCaseTypeProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        serviceToCcdCaseTypeProcessor.process(exchange);
        verify(serviceToCcdCaseTypeProcessor, times(1)).process(exchange);
        List<ServiceToCcdCaseType> resultList = ((List<ServiceToCcdCaseType>) exchange.getMessage().getBody());

        assertEquals(1, resultList.size());
        List<ServiceToCcdCaseType> excepted = ImmutableList.of(
            ServiceToCcdCaseType.builder().ccdCaseType("service1")
                .ccdServiceName("service1 Jurisdiction")
                .serviceCode("1112").build()
        );
        assertEquals(resultList, excepted);
    }

    @Test
    public void testProcessSingleInvalidServiceName() throws Exception {
        exchange.getIn().setBody(ServiceToCcdCaseType.builder().serviceCode("1111")
                                     .ccdServiceName("service1 Jurisdiction").build());
        doNothing().when(serviceToCcdCaseTypeProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        serviceToCcdCaseTypeProcessor.process(exchange);
        verify(serviceToCcdCaseTypeProcessor, times(1)).process(exchange);
        List<ServiceToCcdCaseType> resultList = ((List<ServiceToCcdCaseType>) exchange.getMessage().getBody());

        assertEquals(1, resultList.size());
        List<ServiceToCcdCaseType> excepted = ImmutableList.of(
            ServiceToCcdCaseType.builder()
                .ccdServiceName("service1 Jurisdiction")
                .serviceCode("1111").build()
        );
        assertEquals(resultList, excepted);
    }

    @Test
    public void testProcessSingleInvalidCaseName() throws Exception {
        exchange.getIn().setBody(ServiceToCcdCaseType.builder().serviceCode("1111")
                                     .ccdCaseType("service1").build());
        doNothing().when(serviceToCcdCaseTypeProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        serviceToCcdCaseTypeProcessor.process(exchange);
        verify(serviceToCcdCaseTypeProcessor, times(1)).process(exchange);
        List<ServiceToCcdCaseType> resultList = ((List<ServiceToCcdCaseType>) exchange.getMessage().getBody());

        assertEquals(1, resultList.size());
        List<ServiceToCcdCaseType> excepted = ImmutableList.of(
            ServiceToCcdCaseType.builder()
                .ccdCaseType("service1")
                .serviceCode("1111").build()
        );
        assertEquals(resultList, excepted);
    }

    @Test
    public void testProcessWithOnlyServiceCode() throws Exception {
        exchange.getIn().setBody(ServiceToCcdCaseType.builder().serviceCode("1111").build());
        doNothing().when(serviceToCcdCaseTypeProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        assertThrows(RouteFailedException.class, () -> serviceToCcdCaseTypeProcessor.process(exchange));
        verify(serviceToCcdCaseTypeProcessor, times(1)).process(exchange);
    }

    @Test
    public void testProcessInvalidServiceCodeElementWithException() throws Exception {

        ServiceToCcdCaseType serviceToCcdCaseType = ServiceToCcdCaseType.builder().ccdCaseType("test")
            .ccdServiceName("service1 Jurisdiction").serviceCode("").build();

        exchange.getIn().setBody(serviceToCcdCaseType);
        doNothing().when(serviceToCcdCaseTypeProcessor).audit(serviceToCcdServiceJsrValidatorInitializer, exchange);
        assertThrows(RouteFailedException.class, () -> serviceToCcdCaseTypeProcessor.process(exchange));
        verify(serviceToCcdCaseTypeProcessor, times(1)).process(exchange);
    }
}
