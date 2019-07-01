package com.quadrocompile.jobqueuebackend.annopipeQueues;

import java.util.List;

public class AnnotationSentenceMock {
    private String sentence;
    private List<PipelineStage> nextStages;

    public AnnotationSentenceMock(String sentence,List<PipelineStage> nextStages){
        this.sentence=sentence;
        this.nextStages=nextStages;
    }
}
