package edu.missouristate.aianalyzer.ui.view.Home;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import java.io.File;

public class FileItemModel {
    private final File file;
    private final LongProperty size = new SimpleLongProperty(0);
    private final long totalSpace; // Used for drives

    public FileItemModel(File file) {
        this.file = file;
        if (file.isFile()) {
            this.size.set(file.length());
        }
        // For drives, we can grab total space immediately
        this.totalSpace = (file.getParent() == null) ? file.getTotalSpace() : 0;
    }

    public File getFile() { return file; }
    public String getName() {
        if (file.getParent() == null) return file.getPath(); // Drive letter (C:\)
        return file.getName().isEmpty() ? file.getPath() : file.getName();
    }

    public LongProperty sizeProperty() { return size; }
    public long getSize() { return size.get(); }
    public void setSize(long newSize) { this.size.set(newSize); }

    public long getTotalCapacity() { return totalSpace; } // Only relevant for drives
}