package com.lukaszgajos.filemole.domain.entity.search;

public class Extension implements Filter{
    private String ext;
    public Extension(String extension) {
        ext = extension;
    }

    @Override
    public String getQuery() {
        return "ext = '" + ext + "'";
    }
}
