package edu.missouristate.aianalyzer.ui.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

import java.util.List;

// A simple event we fire off once the main JavaFX window (the Stage) is created.
// Spring beans can listen for this to know when it's safe to start doing UI work.
public class StageReadyEvent extends ApplicationEvent {
    private final Stage stage;
    private List<String> commandLineArgs;

    public StageReadyEvent(Stage stage) {
        super(stage);
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public List<String> getCommandLineArgs() {
        return commandLineArgs;
    }

    public void setCommandLineArgs(List<String> commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }
}
