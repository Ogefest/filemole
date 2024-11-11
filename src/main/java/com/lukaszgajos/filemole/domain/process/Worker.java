package com.lukaszgajos.filemole.domain.process;

public class Worker {

    private WorkerDefinition definition;
    private boolean isFinished = false;

    public Worker(WorkerDefinition definition) {
        this.definition = definition;
    }

    public void start() {
        definition.run();
        isFinished = true;
    }
}
