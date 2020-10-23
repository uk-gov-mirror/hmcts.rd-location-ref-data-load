package uk.gov.hmcts.reform.locationrefdata.camel.listener;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import uk.gov.hmcts.reform.data.ingestion.camel.service.ArchivalBlobServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.service.IArchivalBlobService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class JobResultListenerTest {

    JobResultListener jobResultListener = spy(new JobResultListener());

    JobExecution jobExecution = mock(JobExecution.class);

    IArchivalBlobService archivalBlobService = mock(ArchivalBlobServiceImpl.class);

    @Before
    public void init() {
        setField(jobResultListener, "archivalBlobService", archivalBlobService);
    }

    @Test
    public void beforeJobTest() {
        jobResultListener.beforeJob(jobExecution);
        verify(jobResultListener, times(1)).beforeJob(jobExecution);
    }

    @Test
    public void afterJobTest() {
        doNothing().when(archivalBlobService).executeArchiving();
        jobResultListener.afterJob(jobExecution);
        verify(jobResultListener, times(1)).afterJob(jobExecution);
    }
}
