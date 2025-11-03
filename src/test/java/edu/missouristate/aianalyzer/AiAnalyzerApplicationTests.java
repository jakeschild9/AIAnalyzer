package edu.missouristate.aianalyzer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import edu.missouristate.aianalyzer.config.ServiceLoggingAspect;
import edu.missouristate.aianalyzer.service.database.PassiveScanService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.slf4j.LoggerFactory.getLogger;
@SpringBootTest
@ActiveProfiles("test")
class AiAnalyzerApplicationTests {


    // Prevent watcher
    @MockitoBean
    private PassiveScanService passiveScanService;

    // Mock AI calls

    private ListAppender<ILoggingEvent> appender;

    private void attachAspectListAppender() {
        Logger aspectLogger = (Logger) getLogger(ServiceLoggingAspect.class);
        aspectLogger.setLevel(Level.DEBUG); // ensure DEBUG visible
        appender = new ListAppender<>();
        appender.start();
        aspectLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        Logger aspectLogger = (Logger) getLogger(ServiceLoggingAspect.class);
        aspectLogger.detachAndStopAllAppenders();
    }
}
//    @Test
//    void testAiProcessing() throws Exception {
//        String result = processFile.processFileAIResponse(
//                testFilePath,
//                "txt",
//                8,
//                FileInterpretation.SearchType.ACTIVE
//                );
//        System.out.println(result);
//    }
