package uk.gov.hmcts.reform.locationrefdata.camel.util;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.data.ingestion.camel.service.AuditServiceImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(SpringRunner.class)
public class LrdExecutorTest {

    LrdExecutor lrdExecutor = spy(new LrdExecutor());

    CamelContext camelContext = new DefaultCamelContext();

    AuditServiceImpl auditService = mock(AuditServiceImpl.class);

    ProducerTemplate producerTemplate = mock(ProducerTemplate.class);

    @Before
    public void init() {
        setField(lrdExecutor, "auditService", auditService);
    }

    @Test
    public void testExecute() {
        doNothing().when(producerTemplate).sendBody(any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        lrdExecutor.execute(camelContext, "test", "test");
        verify(lrdExecutor, times(1)).execute(camelContext, "test", "test");
    }

    @Test
    public void testExecuteException() {
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        lrdExecutor.execute(camelContext, "test", "test");
        verify(lrdExecutor, times(1)).execute(camelContext, "test", "test");
    }
}
