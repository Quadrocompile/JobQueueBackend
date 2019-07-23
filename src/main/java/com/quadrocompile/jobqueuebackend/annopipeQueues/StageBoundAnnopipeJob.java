package com.quadrocompile.jobqueuebackend.annopipeQueues;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

public class StageBoundAnnopipeJob implements Callable<StageBoundAnnopipeJob> {
    private final AnnopipeJob job;

    //der stageBound job ist einer eindeutigen pipeline stage zugeordnet (Tokenizer etc.) und wird  nach
    // seiner ausführung nicht mehr benötigt
    private final PipelineStage stage;

    StageBoundAnnopipeJob(AnnopipeJob job, PipelineStage stage){
        this.job=job;
        this.stage=stage;
    }

    public String getJobID(){
        return job.getJobID();
    }

    @Override
    public StageBoundAnnopipeJob call()  {
        //System.out.println("Als Stagebound Job("+stage+") ausgeführt!");
        LinkedBlockingQueue<AnnotationSentenceMock> sentences=job.getStageMap().get(stage);
        List<AnnotationSentenceMock> batch=new ArrayList<>();
        for (int i = 0; i <job.getBatchSize() ; i++) {
            AnnotationSentenceMock nextQueqed=sentences.poll();
            if(nextQueqed!=null){
                batch.add(nextQueqed);
            }
        }
        if(batch.size()>0){
            switch (stage) {
                case TOKENIZER:
                    tokenize(batch);
                    break;
                case TREETAGGER:
                    tag(batch);
                    break;
                case BERKELEY_PARSER:
                    parse(batch);
                    break;
            }
            try {
                pushToNextStage(batch);
                sentences.removeAll(batch);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



            return this;
    }

    //push the processed sentences into the next pipeline stage
    private void pushToNextStage(List<AnnotationSentenceMock> sentences) throws Exception {
        PipelineStage nextStage=job.getNextStage(stage);
        PipelineSlaveScheduler nextStageScheduler= PipelineMasterScheduler.getSchedulerForStage(nextStage);

        //add to schedulers queue, if not present and not finished
        if(!job.getStageMap().containsKey(nextStage)&&nextStage!=PipelineStage.FINISHED){
            nextStageScheduler.addJob(new StageBoundAnnopipeJob(job,nextStage));
            //System.out.println("Job "+getJobID()+" finished in stage "+stage);
        }

        //add to stagemap under the next Stage (key)
        Map<PipelineStage, LinkedBlockingQueue<AnnotationSentenceMock>> stageMap=job.getStageMap();
        if(stageMap.containsKey(nextStage)){
            stageMap.get(nextStage).addAll(sentences);
        }else {
            stageMap.put(nextStage,new LinkedBlockingQueue<>(sentences));
        }
        //System.out.println("Push job "+getJobID()+" to next stage: "+nextStage);

    }

    //overwrite this with the real method!
    private void parse(List<AnnotationSentenceMock> sentences) {
        for (int i = 0; i < sentences.size(); i++) {
            System.out.println(getJobID()+": Parsing sentence "+sentences.get(i));
            AnnotationSentenceMock sentence=sentences.get(i);
            JSONObject sentenceJson=sentence.getJson();
            for (int j = 0; j <sentenceJson.keySet().size() ; j++) {
                JSONObject token=sentenceJson.getJSONObject("ID"+j);
                token.put("PARSE"+j,"PARSE_THAT_SHIT"+j);
            }

        }

    }

    //overwrite this with the real method!
    private void tag(List<AnnotationSentenceMock> sentences) {
        for (int i = 0; i < sentences.size(); i++) {
            System.out.println(getJobID()+": Tagging sentence "+sentences.get(i));
            AnnotationSentenceMock sentence=sentences.get(i);
            JSONObject sentenceJson=sentence.getJson();
            for (int j = 0; j <sentenceJson.keySet().size() ; j++) {
                JSONObject token=sentenceJson.getJSONObject("ID"+j);
                token.put("TAG"+j,"TAG_THAT_SHIT"+j);
            }

        }
    }

    //overwrite this with the real method!
    private void tokenize(List<AnnotationSentenceMock> sentences) {
        for(AnnotationSentenceMock sentence:sentences){
            System.out.println(getJobID()+": Tokenizing sentence '"+sentence.getSentence()+"'");
            String[] tokens=sentence.getSentence().split(" ");
            for (int i = 0; i <tokens.length ; i++) {
                JSONObject token=new JSONObject();
                token.put("TOKEN"+i,tokens[i]);
                JSONObject jsonObject=sentence.getJson();
                jsonObject.put("ID"+i,token);
            }
        }
    }

    public boolean isFinished() {

        if(job.getStageMap().containsKey(stage)){
            return job.getStageMap().get(stage).size()==job.getSentenceList().size()||job.isFinished();
        }
        else {
            //System.out.println("Job "+getJobID()+" not finished yet!");
            return false;}
    }
}
