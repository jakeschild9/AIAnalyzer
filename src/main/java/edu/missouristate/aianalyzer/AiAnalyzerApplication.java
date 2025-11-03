// Springboot entry point
package edu.missouristate.aianalyzer;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import edu.missouristate.aianalyzer.ui.JavaFxApplication;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EntityScan(basePackages = "edu.missouristate.aianalyzer.model.database")
@EnableJpaRepositories(basePackages = "edu.missouristate.aianalyzer.repository.database")
public class AiAnalyzerApplication {
	public static void main(String[] args) {
		Application.launch(JavaFxApplication.class, args);
	}

/**
 * This bean is our startup script. It runs once when the application launches.
 * It will kick off an ACTIVE scan on the user's Downloads folder.
 */
//    @Bean
//    CommandLineRunner startBackgroundIndexer(ActiveScanService indexingService) {
//        return args -> {
//            // For testing, let's just index the user's Test folder. (just create a new \Test folder so this doesn't run forever and crash)
//            Path downloadsFolder = Paths.get(System.getProperty("user.home"), "Test");
//
//            System.out.println("=========================================================");
//            System.out.println("STARTING ACTIVE FILE SCAN OF: " + downloadsFolder);
//            System.out.println("=========================================================");
//
//            indexingService.performActiveScan(List.of(downloadsFolder));
//        };
//    }
}
