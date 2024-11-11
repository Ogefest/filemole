package com.lukaszgajos.filemole.domain.process;

import com.lukaszgajos.filemole.domain.entity.Item;

import java.util.ArrayList;
import java.util.List;

public interface Indexer {
    public List<Item> getItems(Configuration conf);
}
