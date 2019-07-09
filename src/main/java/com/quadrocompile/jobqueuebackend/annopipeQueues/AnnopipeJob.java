package com.quadrocompile.jobqueuebackend.annopipeQueues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;



public class AnnopipeJob implements Callable<AnnopipeJob> {
    private List<AnnotationSentenceMock> sentenceList=new ArrayList<>();

    // zeigt an, welche Sätze sich in welchem Stage befinden; SEHR wichtig. Diese Map ist für die Scheduler wichtig, damit sie
    // wissen, um welche sätze sie sich kümmern müssen!
    final private Map< PipelineStage, List<AnnotationSentenceMock>> stageMap=new ConcurrentHashMap<>();
    final private String jobID;
    final private ArrayList<PipelineStage> stages=new ArrayList<>();
    private int batchSize;

    public AnnopipeJob(String jobID){
        this.jobID=jobID;
        this.batchSize=1;
    }

    public AnnopipeJob(List<AnnotationSentenceMock> sentenceList, String jobID) {
        this(jobID);
        this.sentenceList.addAll(sentenceList);

    }
    public AnnopipeJob(String jobID, List<PipelineStage> stages, List<AnnotationSentenceMock>sentences){
        this(sentences,jobID);
        this.stages.addAll(stages);
        this.stageMap.put(stages.get(0),sentenceList);
    }

    @Override
    public AnnopipeJob call()  {
        System.out.println("Als AnnopipeJob ausgeführt");
        return null;
    }

    public List<AnnotationSentenceMock> getSentenceList() {
        return sentenceList;
    }

    public void setSentenceList(List<AnnotationSentenceMock> sentenceList) {
        this.sentenceList = sentenceList;
    }

    public Map<PipelineStage, List<AnnotationSentenceMock>> getStageMap() {
        return stageMap;
    }

    public String getJobID() {
        return jobID;
    }

    public ArrayList<PipelineStage> getStages() {
        return stages;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public PipelineStage getNextStage(PipelineStage currentStage) throws Exception {
        int index=stages.lastIndexOf(currentStage)+1;
        if(index>0){
            if(index<stages.size()){
                return stages.get(index);
            }else{
                return PipelineStage.FINISHED;
            }
        }else{
            throw new Exception("Stage not in Stage List for Job "+jobID);
        }

    }

    public boolean isFinished(){
        if(stageMap.containsKey(PipelineStage.FINISHED)){
            return stageMap.get(PipelineStage.FINISHED).size()==sentenceList.size();
        }
        else {
            System.out.println("Job "+jobID+" not finished yet!");
            return false;}
    }
}
