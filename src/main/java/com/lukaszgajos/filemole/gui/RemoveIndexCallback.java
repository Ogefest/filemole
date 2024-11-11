package com.lukaszgajos.filemole.gui;

import com.lukaszgajos.filemole.domain.entity.IndexDefinition;
import com.lukaszgajos.filemole.domain.service.IndexService;

public interface RemoveIndexCallback {
    public void remove(IndexDefinition indexDefinition);
}
