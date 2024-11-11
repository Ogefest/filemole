package com.lukaszgajos.filemole.domain.entity;

import java.util.ArrayList;
import java.util.List;

public class IndexDefinition {
    private String name;
    private int id;
    private long itemsSize;
    private long itemsCount;
    private boolean isEnabledForSearch = true;

    public IndexDefinition(int id, String name, long itemsCount, long itemsSize) {
        this.name = name;
        this.id = id;
        this.itemsSize = itemsSize;
        this.itemsCount = itemsCount;
    }

    public boolean isEnabledForSearch() {
        return isEnabledForSearch;
    }

    public void setEnabledForSearch(boolean enabledForSearch) {
        isEnabledForSearch = enabledForSearch;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public long getItemsSize() {
        return itemsSize;
    }

    public long getItemsCount() {
        return itemsCount;
    }
}
