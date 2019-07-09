package com.quadrocompile.jobqueuebackend.model;

import com.quadrocompile.jobqueuebackend.annopipeQueues.AnnotationSentenceMock;
import com.quadrocompile.jobqueuebackend.annopipeQueues.PipelineStage;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Callable;

public class MockAnnopipeJobForPipelineStage implements Callable<MockAnnopipeJobForPipelineStage> {
    private MockAnnopipeJob job;
    private PipelineStage stage;

    MockAnnopipeJobForPipelineStage(MockAnnopipeJob job, PipelineStage stage){
        this.job=job;
        this.stage=stage;

    }

    @Override
    public MockAnnopipeJobForPipelineStage call()  {
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
        pushToNextStage(batch);
        sentences.removeAll(batch);
        return null;
    }

    private void pushToNextStage(List<AnnotationSentenceMock> sentences) {

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
