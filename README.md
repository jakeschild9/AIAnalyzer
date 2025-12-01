<p align="center">
  <img src="README_images/logo.png" alt="AiAnalyzer Logo" width="120">
</p>

<h1 id="aianalyzer" align="center">File AiAnalyzer</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue?logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaFX-21-orange?logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/SQLite-Database-blue?logo=sqlite&logoColor=white" />
  <img src="https://img.shields.io/badge/License-MIT-yellow?logo=open-source-initiative&logoColor=white" />
</p>

<hr style="border: 0.5px solid #ccc; margin: 20px 0;">

<!-- Main large image -->
<p align="center">
  <img src="README_images/img_2.png" alt="AIAnalyzer Main Dashboard" width="650"><br>
  <em>Main Dashboard Interface</em>
</p>

<!-- Two smaller images side by side -->
<p align="center">
  <img src="README_images/theme_selection.png" alt="Theme Selection" width="320" style="margin: 0 10px;">
  <img src="README_images/metrics.png" alt="Metrics" width="320" style="margin: 0 10px;"><br>
  <em>Theme Selection (left) &nbsp;&nbsp;•&nbsp;&nbsp; Metric Reports (right)</em>
</p>

<p align="center">
  <a href="#getting-started--setup">Getting Started</a> •
  <a href="#project-structure">Project Structure</a> •
  <a href="#technology-stack">Tech Stack</a> •
  <a href="#how-to-run">How to Run</a> •
  <a href="#contributors">Contributors</a>
</p>

## Overview
File AiAnalyzer is a desktop application that scans local files, applies AI-driven analysis (including image understanding and malware checks), and persists results and metrics. It pairs a JavaFX user interface with a Spring Boot backend and SQLite database.

## Key Features
- File System Scanning (active and background queues)
- AI-Powered Classification (files and images)
- Desktop UI (JavaFX) with theming
- Metrics and reporting (counts, throughput, user actions)
- Persistent storage (SQLite via Spring Data JPA)

<p style="text-align: right;">
  <a href="#aianalyzer">Back to Top ↑</a>
</p>

## Getting Started & Setup
First-time setup is required. Configure your AI credentials and database.

See:
- README_AI.md — AI service setup and usage
- README_DATABASE.md — Database configuration, schema, and tips

### Quick start (Windows / IntelliJ)
1. Open the project in IntelliJ IDEA.
2. Ensure JDK 21 and JavaFX are configured in the IDE.
3. Run the main class: `src/main/java/edu/missouristate/aianalyzer/AiAnalyzerApplication.java`
4. Maven:
   - Build: `mvn clean package`
   - Run (dev): `mvn spring-boot:run`
   - Run jar: `java -jar target/<artifact>-<version>.jar`
5. Tests: `mvn test`

### Common paths (fast links)
- Properties: `src/main/resources/application.properties`
- Logging: `src/main/resources/logback.xml`
- UI (views, services): `src/main/java/edu/missouristate/aianalyzer/ui`
- AI services: `src/main/java/edu/missouristate/aianalyzer/service/ai`
- Persistence: `src/main/java/edu/missouristate/aianalyzer/model/database` and `repository/database`
- Metrics: `src/main/java/edu/missouristate/aianalyzer/service/metrics`

<p style="text-align: right;">
  <a href="#aianalyzer">Back to Top ↑</a>
</p>

## Project Structure
<details>
<summary>Click to view the collapsed project structure</summary>

```
src/main/
├── java/
│   └── edu/missouristate/aianalyzer/
│       ├── AiAnalyzerApplication.java    <- [Entry Point]
│       ├── config/..                     <- Spring @Configuration beans and AI client setup.
│       ├── model/..                      <- Data structures and JPA @Entity classes for database tables.
│       ├── repository/..                 <- Spring Data JPA repositories (query interfaces).
│       ├── service/..                    <- Core backend business logic (AI, DB operations, metrics).
│       ├── ui/..                         <- JavaFX frontend code (views, events, services).
│       └── utility/..                    <- Helper utilities for AI operations and file handling.
└── resources/..                          <- Config files, CSS themes, and logging setup.
```

</details>


<details>
<summary>Click to view the expanded project structure</summary>

```
src/main/
├── java/
│   └── edu/missouristate/aianalyzer/
│       ├── AiAnalyzerApplication.java            <- The main entry point; its main() starts Spring Boot and the entire app.
│       ├── config/
│       │   ├── AiClient.java                     <- Sets up and configures the Google Gemini AI client.
│       │   └── ServiceLoggingAspect.java         <- AOP aspect for cross-cutting service method logging.
│       ├── model/
│       │   ├── FileInterpretation.java           <- Domain model describing AI output and file classification.
│       │   └── database/
│       │       ├── DecisionType.java             <- Enum for user decisions (Ignore, Quarantine, Delete).
│       │       ├── ErrorLog.java                 <- Entity for tracking processing errors.
│       │       ├── FileRecord.java               <- Main table; stores metadata and AI analysis results.
│       │       ├── FileTypeMetrics.java          <- Aggregated metrics per file type/extension.
│       │       ├── ImageMeta.java                <- Specialized metadata extracted from image files.
│       │       ├── LabelHistory.java             <- History of AI-generated labels for each file.
│       │       ├── MetricsSummary.java           <- System-wide aggregated counters and metrics.
│       │       ├── PreferenceAudit.java          <- Audit trail of user preference changes.
│       │       ├── ScanQueueItem.java            <- A queued work item representing a file waiting to be analyzed.
│       │       ├── UserDecisionLog.java          <- Logs user actions on files (ignore, quarantine, delete).
│       │       ├── UserPreference.java           <- Stores user-configurable settings.
│       │       └── VirusScan.java                <- Virus scan results per file.
│       ├── repository/
│       │   └── database/
│       │       ├── ErrorLogRepository.java       <- CRUD operations for ErrorLog.
│       │       ├── FileRecordRepository.java     <- Persistence and queries for FileRecord.
│       │       ├── FileTypeMetricsRepository.java<- CRUD operations for FileTypeMetrics.
│       │       ├── ImageMetaRepository.java      <- Persistence for ImageMeta.
│       │       ├── LabelHistoryRepository.java   <- CRUD operations for LabelHistory.
│       │       ├── MetricsSummaryRepository.java <- CRUD operations for MetricsSummary.
│       │       ├── PreferenceAuditRepository.java<- CRUD operations for PreferenceAudit.
│       │       ├── ScanQueueItemRepository.java  <- Persistence for ScanQueueItem.
│       │       ├── UserDecisionLogRepository.java<- CRUD operations for UserDecisionLog.
│       │       ├── UserPreferenceRepository.java <- CRUD operations for UserPreference.
│       │       └── VirusScanRepository.java      <- Persistence for VirusScan.
│       ├── service/
│       │   ├── CloudConfigService.java           <- Manages Google Cloud configuration (project, bucket, credentials).
│       │   ├── ai/
│       │   │   ├── ProcessFileService.java       <- AI processing for general files; handles large-file chunking.
│       │   │   ├── ProcessImageService.java      <- AI processing for image files.
│       │   │   └── ScanForVirusService.java      <- Integrates virus scanning into processing workflow.
│       │   ├── database/
│       │   │   ├── ActiveScanService.java        <- Producer adding files to the queue on user-initiated scans.
│       │   │   ├── ErrorLogService.java          <- Persists and retrieves system error logs.
│       │   │   ├── ErrorRetryWorker.java         <- Background worker retrying failed tasks.
│       │   │   ├── FileIsolationService.java     <- Handles quarantine and restoration operations.
│       │   │   ├── FileProcessingService.java    <- Consumer processing queued items; orchestrates AI + persistence.
│       │   │   ├── LabelService.java             <- Parses AI responses and applies classification labels.
│       │   │   ├── PreferenceChangedEvent.java   <- Event fired when user preferences are changed.
│       │   │   ├── PreferenceService.java        <- Reads/writes preferences; publishes change events.
│       │   │   ├── SearchService.java            <- Search/filter operations across file records.
│       │   │   └── VirusScanService.java         <- Coordinates and persists virus scan results.
│       │   ├── metrics/
│       │   │   ├── HomeMetricsService.java       <- Aggregates metrics for the home screen.
│       │   │   ├── MetricsAggregationService.java<- Aggregates data for dashboards, charts, and reports.
│       │   │   └── MetricsService.java           <- Helpers and utilities for metrics calculations.
│       │   └── photos/
│       │       └── FindDuplicatesService.java    <- Finds duplicate photos via perceptual hashing.
│       ├── ui/
│       │   ├── JavaFxApplication.java            <- Bootstraps JavaFX; bridges JavaFX lifecycle with Spring Boot.
│       │   ├── StageInitializer.java             <- Builds primary stage, loads scenes, shows first view.
│       │   ├── event/
│       │   │   └── StageReadyEvent.java          <- Event fired when JavaFX stage is ready.
│       │   ├── service/
│       │   │   ├── FileSystemService.java        <- File system utilities for UI directory and metadata retrieval.
│       │   │   ├── ThemeManager.java             <- Discovers installed themes and exposes theme palettes.
│       │   │   ├── ThemeService.java             <- Applies and switches UI themes.
│       │   │   └── UIUpdateService.java          <- Batches UI updates on the JavaFX application thread.
│       │   └── view/
│       │       ├── Home/
│       │       │   ├── CategoryCard.java         <- Reusable card component for file category metrics.
│       │       │   ├── DriveView.java            <- Main home screen: drives, categories, explorer.
│       │       │   ├── ExplorerFileCell.java     <- Custom renderer for file items (status, size, actions).
│       │       │   ├── FileItemModel.java        <- Model for file/folder items in the UI tree.
│       │       │   └── FileTreeItem.java         <- Lazy-loads folder contents; computes sizes in background.
│       │       ├── Metrics/
│       │       │   ├── MetricCard.java           <- Dashboard card w/ sparklines for metric visualization.
│       │       │   └── MetricsView.java          <- Metrics dashboard screen (charts, counts, throughput).
│       │       ├── Settings/
│       │       │   └── SettingsView.java         <- Settings screen for themes and cloud configuration.
│       │       └── Suggestions/
│       │           ├── SuggestionsItemCell.java  <- Custom cell renderer for AI suggestions.
│       │           └── SuggestionsView.java      <- AI-powered recommendations for file cleanup.
│       └── utility/
│           └── ai/
│               ├── AiQueryUtil.java              <- Helper for forming/sending prompts to the AI service.
│               ├── ClamDownloadUtil.java         <- Downloads and installs ClamAV.
│               ├── ImageMagickDownloadUtil.java  <- Downloads/configures ImageMagick for image processing.
│               ├── ReadFileUtil.java             <- Reads and chunks large files for AI processing.
│               ├── ReadImageUtil.java            <- Prepares image files for AI vision analysis.
│               └── UploadFileUtil.java           <- Uploads files to Google Cloud Storage.
└── resources/
    ├── application.properties                     <- Spring Boot config (DB, logging, feature flags, cloud).
    ├── logback.xml                                <- Structured logging configuration.
    └── styles/                                    <- CSS theme files for JavaFX.
```

</details>
