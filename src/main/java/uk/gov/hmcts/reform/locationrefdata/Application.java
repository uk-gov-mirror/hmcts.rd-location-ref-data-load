package uk.gov.hmcts.reform.locationrefdata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = "uk.gov.hmcts.reform")
@SuppressWarnings ("PMD.DoNotCallSystemExit")
@Slf4j
public class Application implements ApplicationRunner {

    private static String logComponentName = "Location Reference Data Load";

    public static void main(final String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class);
        //Sleep added to allow app-insights to flush the logs
        // Thread.sleep(7000);
        int exitCode = SpringApplication.exit(context);
        log.info("{}:: Application exiting with exit code {} ", logComponentName, exitCode);
        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("{}:: no run of  Application as it has ran for the day::", logComponentName);
    }
}
