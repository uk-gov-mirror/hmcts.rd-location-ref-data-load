package uk.gov.hmcts.reform.locationrefdata.camel.listener;

import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import uk.gov.hmcts.reform.data.ingestion.camel.route.ArchivalRoute;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class JobResultListenerTest {

    JobResultListener jobResultListener = spy(new JobResultListener());

    JobExecution jobExecution = mock(JobExecution.class);

    ArchivalRoute archivalRoute = mock(ArchivalRoute.class);

    ProducerTemplate producerTemplate = mock(ProducerTemplate.class);

    @Before
    public void init() {
        setField(jobResultListener, "producerTemplate", producerTemplate);
        setField(jobResultListener, "archivalRoute", archivalRoute);
    }

    @Test
    public void beforeJobTest() {
        jobResultListener.beforeJob(jobExecution);
        verify(jobResultListener, times(1)).beforeJob(jobExecution);
    }

    @Test
    public void afterJobTest() {
        doNothing().when(producerTemplate).sendBody(any());
        doNothing().when(archivalRoute).archivalRoute(any());
        jobResultListener.afterJob(jobExecution);
        verify(jobResultListener, times(1)).afterJob(jobExecution);
    }
}
