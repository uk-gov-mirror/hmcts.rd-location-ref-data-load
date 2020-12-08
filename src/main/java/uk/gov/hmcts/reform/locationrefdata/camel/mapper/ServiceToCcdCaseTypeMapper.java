package uk.gov.hmcts.reform.locationrefdata.camel.mapper;

import static java.util.Objects.nonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.ServiceToCcdCaseType;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServiceToCcdCaseTypeMapper implements IMapper {

    @Override
    public Map<String, Object> getMap(Object serviceToCcdService) {
        ServiceToCcdCaseType serviceToCcdCaseTypeType = (ServiceToCcdCaseType) serviceToCcdService;
        Map<String, Object> serviceToCcdServiceRow = new HashMap<>();
        serviceToCcdServiceRow.put("service_code", trim(serviceToCcdCaseTypeType.getServiceCode()));
        serviceToCcdServiceRow.put("ccd_case_type", trim(serviceToCcdCaseTypeType.getCcdCaseType()));
        serviceToCcdServiceRow.put("ccd_service_name", trim(serviceToCcdCaseTypeType.getCcdServiceName()));
        return serviceToCcdServiceRow;
    }

    public String trim(String value) {
        return nonNull(value) ? value.trim() : null;
    }
}
