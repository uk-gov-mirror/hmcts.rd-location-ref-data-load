package uk.gov.hmcts.reform.locationrefdata.camel.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.locationrefdata.camel.util.LrdExecutor;

import java.util.List;

@Component
@Slf4j
public class LrdRouteTask implements Tasklet {

    @Value("${start-route}")
    private String startRoute;

    @Autowired
    CamelContext camelContext;

    @Autowired
    LrdExecutor lrdExecutor;

    @Autowired
    DataLoadRoute dataLoadRoute;

    @Value("${routes-to-execute}")
    List<String> routesToExecute;

    @Value("${logging-component-name}")
    private String logComponentName;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("{}:: ParentRouteTask starts::", logComponentName);
        dataLoadRoute.startRoute(startRoute, routesToExecute);
        String status = lrdExecutor.execute(camelContext, "LRD Route", startRoute);
        log.info("{}:: ParentRouteTask completes with status::{}", logComponentName, status);
        return RepeatStatus.FINISHED;
    }
}
