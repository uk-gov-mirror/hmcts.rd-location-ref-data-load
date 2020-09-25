package uk.gov.hmcts.reform.locationrefdata.camel.mapper;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ServiceToCcdServiceMapperTest {

    ServiceToCcdServiceMapper serviceToCcdServiceMapper = spy(new ServiceToCcdServiceMapper());

    @Test
    public void testGetMap() {
        ServiceToCcdService serviceToCcdService = ServiceToCcdService.builder().serviceCode("test")
            .ccdServiceName("service1,service2").ccdJurisdictionName("testJurisdiction").build();
        Map<String, Object> resultMap = serviceToCcdServiceMapper.getMap(serviceToCcdService);
        assertEquals(resultMap, ImmutableMap.of("service_code", "test",
                                                "ccd_service_name", "service1,service2",
                                                "ccd_jurisdiction_name", "testJurisdiction"));
        verify(serviceToCcdServiceMapper, times(1)).getMap(serviceToCcdService);
    }
}
