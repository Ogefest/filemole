package com.lukaszgajos.filemole.gui;

import com.lukaszgajos.filemole.domain.entity.IndexDefinition;

public interface ChangeIndexEnabeldForSearchCallback {
    public void changeEnabled(IndexDefinition index, boolean state);
}
