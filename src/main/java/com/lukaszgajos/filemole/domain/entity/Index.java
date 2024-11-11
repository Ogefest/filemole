package com.lukaszgajos.filemole.domain.entity;

import java.util.List;

public class Index {
    private IndexDefinition definition;
    private List<Item> items;

    public Index(IndexDefinition definition, List<Item> items){
        this.definition = definition;
        this.items = items;
    }

    public IndexDefinition getDefinition() {
        return definition;
    }

    public List<Item> getItems() {
        return items;
    }

}
