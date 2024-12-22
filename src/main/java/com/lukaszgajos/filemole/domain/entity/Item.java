package com.lukaszgajos.filemole.domain.entity;

import java.io.File;

public class Item {
    public String path;
    public String name;
    public long size;
    public long lastModified;
    public long createdAt;
    public String ext;
    public boolean isDir = false;
    public String archive;
}
