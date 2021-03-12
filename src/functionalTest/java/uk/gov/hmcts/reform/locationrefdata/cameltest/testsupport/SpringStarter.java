package uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

public class SpringStarter {

    private static SpringStarter INSTANCE = null;

    private TestContextManager testContextManager;

    public static SpringStarter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpringStarter();
        }
        return INSTANCE;
    }

    public void init(TestContextManager testContextManager) {
        this.testContextManager = testContextManager;
    }

    public void restart() {
        testContextManager.getTestContext().markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
        reInjectDependencies();
    }

    private void reInjectDependencies() {
        testContextManager
            .getTestExecutionListeners()
            .stream()
            .filter(listener -> listener instanceof DependencyInjectionTestExecutionListener)
            .findFirst()
            .ifPresent(listener -> {
                try {
                    listener.prepareTestInstance(testContextManager.getTestContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

}
