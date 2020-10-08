package uk.gov.hmcts.reform.locationrefdata.camel.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.service.AuditServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.util.RouteExecutor;

import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ERROR_MESSAGE;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.FAILURE;

@Slf4j
@Component
@RefreshScope
public class LrdExecutor extends RouteExecutor {

    @Value("${logging-component-name}")
    private String logComponentName;

    @Autowired
    AuditServiceImpl auditService;

    @Override
    public String execute(CamelContext camelContext, String schedulerName, String route) {
        try {
            return super.execute(camelContext, schedulerName, route);

        } catch (Exception ex) {
            String errorMessage = camelContext.getGlobalOptions().get(ERROR_MESSAGE);
            auditService.auditException(camelContext,errorMessage);
            log.error("{}:: {} failed:: {}", logComponentName, schedulerName, errorMessage);
            return FAILURE;
        } finally {
            auditService.auditSchedulerStatus(camelContext);
        }
    }
}
