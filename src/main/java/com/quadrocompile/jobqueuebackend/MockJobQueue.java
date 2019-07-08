package com.quadrocompile.jobqueuebackend;

import com.quadrocompile.jobqueuebackend.annopipeQueues.AnnotationSentenceMock;
import com.quadrocompile.jobqueuebackend.annopipeQueues.PipelineStage;

import java.util.concurrent.LinkedBlockingDeque;

public class MockJobQueue {
    private PipelineStage pipelineStage;
    private final LinkedBlockingDeque<AnnotationSentenceMock> sumbitQueue = new LinkedBlockingDeque<>();

    public MockJobQueue(PipelineStage pipelineStage) {
        this.pipelineStage = pipelineStage;
    }
}
