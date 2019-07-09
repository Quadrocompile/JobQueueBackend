package com.quadrocompile.jobqueuebackend.annopipeQueues;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;

// ist zuständig für deine einzelne Stage der Jobs (Tokenizer, TreeTagger etc.)
public class PipelineSlaveScheduler implements Runnable {
    private PipelineStage pipelineStage;
    private final List<FutureTask<StageBoundAnnopipeJob>> jobTasks = new LinkedList<>();
    private final LinkedBlockingDeque<StageBoundAnnopipeJob> sumbitQueue = new LinkedBlockingDeque<>();

    public PipelineSlaveScheduler(PipelineStage pipelineStage) {
        this.pipelineStage = pipelineStage;
    }

    public PipelineStage getPipelineStage() {
        return pipelineStage;
    }
    public void addJob(StageBoundAnnopipeJob newJob){
        sumbitQueue.offer(newJob);
    }


    @Override
    public void run() {

    }
}
