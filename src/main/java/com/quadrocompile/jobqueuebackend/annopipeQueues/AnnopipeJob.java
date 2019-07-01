package com.quadrocompile.jobqueuebackend.annopipeQueues;

import java.util.List;
import java.util.concurrent.Callable;

public class AnnopipeJob implements Callable<AnnopipeJob> {
    List<AnnotationSentenceMock> sentenceList;

    @Override
    public AnnopipeJob call()  {
        return null;
    }
}
