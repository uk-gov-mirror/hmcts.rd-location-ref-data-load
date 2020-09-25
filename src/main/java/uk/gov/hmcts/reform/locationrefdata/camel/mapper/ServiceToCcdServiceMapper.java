package uk.gov.hmcts.reform.locationrefdata.camel.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdService;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServiceToCcdServiceMapper implements IMapper {

    @Override
    public Map<String, Object> getMap(Object serviceToCcdService) {
        ServiceToCcdService serviceToCcdServiceType = (ServiceToCcdService) serviceToCcdService;
        Map<String, Object> serviceToCcdServiceRow = new HashMap<>();
        serviceToCcdServiceRow.put("service_code", serviceToCcdServiceType.getServiceCode());
        serviceToCcdServiceRow.put("ccd_service_name", serviceToCcdServiceType.getCcdServiceName());
        serviceToCcdServiceRow.put("ccd_jurisdiction_name", serviceToCcdServiceType.getCcdJurisdictionName());
        return serviceToCcdServiceRow;
    }
}
