package com.quadrocompile.jobqueuebackend.model;

import com.quadrocompile.jobqueuebackend.annopipeQueues.AnnotationSentenceMock;
import com.quadrocompile.jobqueuebackend.annopipeQueues.PipelineStage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class MockAnnopipeJob implements Callable<MockAnnopipeJob> {
    private List<AnnotationSentenceMock> sentenceList=new ArrayList<>();
    private Map<Integer, PipelineStage> sentence_status=new HashMap<>();
    private String jobID;
    private ArrayList<PipelineStage> stages=new ArrayList<>();

    public MockAnnopipeJob(String jobID){
        this.jobID=jobID;
    }

    public MockAnnopipeJob(List<AnnotationSentenceMock> sentenceList, String jobID) {
        this(jobID);
        this.sentenceList.addAll(sentenceList);

    }
    public MockAnnopipeJob(String jobID,List<PipelineStage> stages, List<AnnotationSentenceMock>sentences){
       this(sentences,jobID);
       this.stages.addAll(stages);
    }

    @Override
    public MockAnnopipeJob call()  {
        return null;
    }
}
