package com.whatsapp;

import java.io.File;
import java.io.Serializable;

public class MediaData implements Serializable {
    @SuppressWarnings("UnusedDeclaration")
    static final long serialVersionUID = -3211751283609594L;

    File file;
    long fileSize;
    boolean transferred;
    long progress;

    @Override
    public String toString() {
        return "MediaData{" +
                "file=" + file +
                ", fileSize=" + fileSize +
                ", transferred=" + transferred +
                ", progress=" + progress +
                '}';
    }


    public File getFile() {
        return file;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isTransferred() {
        return transferred;
    }

    public long getProgress() {
        return progress;
    }
}
