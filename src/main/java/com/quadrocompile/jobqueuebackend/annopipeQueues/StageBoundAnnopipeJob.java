package com.quadrocompile.jobqueuebackend.annopipeQueues;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class StageBoundAnnopipeJob implements Callable<StageBoundAnnopipeJob> {
    private AnnopipeJob job;
    private PipelineStage stage;

    StageBoundAnnopipeJob(AnnopipeJob job, PipelineStage stage){
        this.job=job;
        this.stage=stage;

    }

    @Override
    public StageBoundAnnopipeJob call()  {
        List<AnnotationSentenceMock> sentences=job.getStageMap().get(stage);

        List<AnnotationSentenceMock>batch=sentences.subList(0,Math.min(job.getBatchSize(),sentences.size()));

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

        return this;
    }

    //push the processed sentences into the next pipeline stage
    private void pushToNextStage(List<AnnotationSentenceMock> sentences) throws Exception {
        PipelineStage nextStage=job.getNextStage(stage);
        PipelineSlaveScheduler nextStageScheduler= PipelineMasterScheduler.getSchedulerForStage(nextStage);

        //add to schedulers queue, if not present and not finished
        if(!job.getStageMap().containsKey(nextStage)&&nextStage!=PipelineStage.FINISHED){
            nextStageScheduler.addJob(new StageBoundAnnopipeJob(job,nextStage));
        }

        //add to stagemap under the next Stage (key)
        Map<PipelineStage, List<AnnotationSentenceMock>> stageMap=job.getStageMap();
        if(stageMap.containsKey(nextStage)){
                stageMap.get(nextStage).addAll(sentences);
        }else {
            stageMap.put(nextStage,sentences);
        }

    }

    //overwrite this with the real method!
    private void parse(List<AnnotationSentenceMock> sentences) {
        for (int i = 0; i < sentences.size(); i++) {
            JSONObject jsonObject=sentences.get(i).getJson().getJSONObject("TOKEN"+i);
            jsonObject.put("PARSE"+i,"PARSE_THAT_SHIT"+i);
        }

    }

    //overwrite this with the real method!
    private void tag(List<AnnotationSentenceMock> sentences) {
        for (int i = 0; i < sentences.size(); i++) {
            JSONObject jsonObject=sentences.get(i).getJson().getJSONObject("TOKEN"+i);
            jsonObject.put("TAG"+i,"PARSE_THAT_SHIT"+i);
        }
    }

    //overwrite this with the real method!
    private void tokenize(List<AnnotationSentenceMock> sennteces) {
        for(AnnotationSentenceMock sentence:sennteces){
            String[] tokens=sentence.getSentence().split(" ");
            for (int i = 0; i <tokens.length ; i++) {
                JSONObject jsonObject=sentence.getJson();
                jsonObject.put("TOKEN"+i,tokens[i]);
            }
        }
    }
}
