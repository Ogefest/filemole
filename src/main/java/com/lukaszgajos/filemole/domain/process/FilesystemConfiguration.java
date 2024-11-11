package com.lukaszgajos.filemole.domain.process;

public class FilesystemConfiguration implements Configuration {
    private String startPath;

    public FilesystemConfiguration(String initPath) {
        startPath = initPath;
    }

    public String getStartPath() {
        return startPath;
    }

}
