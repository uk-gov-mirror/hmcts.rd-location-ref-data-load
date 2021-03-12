package uk.gov.hmcts.reform.locationrefdata.camel.util;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.data.ingestion.camel.service.AuditServiceImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(SpringExtension.class)
class LrdExecutorTest {

    LrdExecutor lrdExecutor = new LrdExecutor();

    LrdExecutor lrdExecutorSpy = spy(lrdExecutor);

    CamelContext camelContext = new DefaultCamelContext();

    AuditServiceImpl auditService = mock(AuditServiceImpl.class);

    ProducerTemplate producerTemplate = mock(ProducerTemplate.class);

    @BeforeEach
    public void init() {
        setField(lrdExecutorSpy, "auditService", auditService);
    }

    @Test
    void testExecute() {
        doNothing().when(producerTemplate).sendBody(any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        lrdExecutorSpy.execute(camelContext, "test", "test");
        verify(lrdExecutorSpy, times(1)).execute(camelContext, "test", "test");
    }

    @Test
    void testExecuteException() {
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        lrdExecutorSpy.execute(camelContext, "test", "test");
        verify(lrdExecutorSpy, times(1)).execute(camelContext, "test", "test");
    }
}
