package com.quadrocompile.jobqueuebackend.annopipeQueues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class AnnopipeJob implements Callable<AnnopipeJob> {

    private List<AnnotationSentenceMock> sentenceList=new ArrayList<>();

    // zeigt an, welche Sätze sich in welchem Stage befinden; SEHR wichtig. Diese Map ist für die Scheduler wichtig, damit sie
    // wissen, um welche sätze sie sich kümmern müssen!
    final private Map< PipelineStage, LinkedBlockingQueue<AnnotationSentenceMock>> stageMap=new ConcurrentHashMap<>();
    final private String jobID;
    final private ArrayList<PipelineStage> stages=new ArrayList<>();
    private int batchSize;

    public AnnopipeJob(String jobID){
        this.jobID=jobID+"@"+System.currentTimeMillis();
        this.batchSize=1;
    }

    public AnnopipeJob(List<AnnotationSentenceMock> sentenceList, String jobID) {
        this(jobID);
        this.sentenceList.addAll(sentenceList);

    }
    public AnnopipeJob(String jobID, List<PipelineStage> stages, List<AnnotationSentenceMock>sentences){
        this(sentences,jobID);
        this.stages.addAll(stages);
    }

    @Override
    public AnnopipeJob call()  {
        //System.out.println("Als AnnopipeJob ausgeführt");

        //falls der job neu in den master-scheduler gekommen ist, muss er zunächst dem ersten stage übergeben werden
        // das kann variabel passieren (wenn der Job schon vorher tokenisiert wurde und nun noch dazu getaggt werden soll z.B.)
        PipelineStage firstStage=stages.get(0);
        if(!stageMap.containsKey(firstStage)){
            //falls der job noch nicht in den ersten stage gelangt ist, muss man ihn erstellen
            stageMap.put(firstStage,new LinkedBlockingQueue<>(sentenceList));
            //für jeden stage wird ein neuer StageboundJob erstellt, der den eigentlichen job wrapt
            PipelineMasterScheduler.getSchedulerForStage(firstStage).addJob(new StageBoundAnnopipeJob(this,firstStage));
            //System.out.println("An Scheduler für "+firstStage+" übergeben.");
        }else{
            //System.out.println("Job "+jobID+" schon in der Pipeline vorhanden!");
        }

        // Return reference to this job
        return this;
    }

    public List<AnnotationSentenceMock> getSentenceList() {
        return sentenceList;
    }

    public void setSentenceList(List<AnnotationSentenceMock> sentenceList) {
        this.sentenceList = sentenceList;
    }

    public Map<PipelineStage, LinkedBlockingQueue<AnnotationSentenceMock>> getStageMap() {
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
            //System.out.println("Job "+jobID+" not finished yet!");
            return false;}
    }

    @Override
    public String toString() {
        return sentenceList.toString();
    }
}
