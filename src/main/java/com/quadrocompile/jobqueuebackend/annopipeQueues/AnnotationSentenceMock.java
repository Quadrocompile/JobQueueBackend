package com.quadrocompile.jobqueuebackend.annopipeQueues;

import org.json.JSONObject;

public class AnnotationSentenceMock {
    private String sentence;
    private JSONObject json;

    public AnnotationSentenceMock(String sentence){
        this.sentence=sentence;
        this.json=new JSONObject();
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
