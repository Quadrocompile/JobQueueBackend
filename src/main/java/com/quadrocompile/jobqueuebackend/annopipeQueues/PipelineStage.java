package com.quadrocompile.jobqueuebackend.annopipeQueues;

public enum PipelineStage {

    TOKENIZER("Tokenizer"),
    TREETAGGER("Treetagger"),
    BERKELEY_PARSER("Berkeley Parser");

    String type;
    PipelineStage(String type) {
        this.type=type;
    }
    @Override
    public String toString(){
        return this.type;
    }
    public int toInt(){
        return this.ordinal();
    }
}
