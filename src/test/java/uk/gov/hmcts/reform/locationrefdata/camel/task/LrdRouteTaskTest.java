package uk.gov.hmcts.reform.locationrefdata.camel.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.repeat.RepeatStatus;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.locationrefdata.camel.util.LrdExecutor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class LrdRouteTaskTest {

    LrdRouteTask lrdRouteTask = spy(new LrdRouteTask());

    DataLoadRoute dataLoadRoute = mock(DataLoadRoute.class);

    LrdExecutor lrdExecutor = mock(LrdExecutor.class);

    @BeforeEach
    public void init() {
        setField(lrdRouteTask, "logComponentName", "testlogger");
        setField(lrdRouteTask, "dataLoadRoute", dataLoadRoute);
        setField(lrdRouteTask, "lrdExecutor", lrdExecutor);
    }

    @Test
    void testExecute() throws Exception {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(lrdExecutor.execute(any(), any(), any())).thenReturn("success");
        assertEquals(RepeatStatus.FINISHED, lrdRouteTask.execute(any(), any()));
        verify(lrdRouteTask, times(1)).execute(any(), any());
    }
}
