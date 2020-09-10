package uk.gov.hmcts.reform.locationrefdata;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

public class ApplicationTest {


    @Test
    public void mainTest() {
        Application.main(new String[] {});
        verifyStatic(Application.class,times(1));
    }
}
