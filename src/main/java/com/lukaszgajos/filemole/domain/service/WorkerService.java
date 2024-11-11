package com.lukaszgajos.filemole.domain.service;

import com.lukaszgajos.filemole.domain.process.Worker;
import com.lukaszgajos.filemole.domain.process.WorkerDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerService {
    private List<Worker> workerList = new ArrayList<>();

    public List<Worker> getWorkerList() {
        return workerList;
    }

    public void registerNewJob(Worker worker) {
        workerList.add(worker);
        Executors.newSingleThreadScheduledExecutor().submit(worker::start);
    }
}
