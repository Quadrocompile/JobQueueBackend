package com.quadrocompile.jobqueuebackend.scheduler;

import com.quadrocompile.jobqueuebackend.annopipeQueues.AnnotationSentenceMock;
import com.quadrocompile.jobqueuebackend.model.MockAnnopipeJob;

import java.util.ArrayList;
import java.util.List;

public class MockAnnopipeJobScheduler {
    private static String[] animals={"Kater","Hund","Kolibri","Alligator","Ameisenbär","Eichenprozessionsspinner","Uhu","Elefant"};

    public static void main(String[] args) {


        List<AnnotationSentenceMock> sentences1=createMockSentences(8,"Ich");
        List<AnnotationSentenceMock> sentences2=createMockSentences(4,"Peter");
        MockAnnopipeJob job1=new MockAnnopipeJob(sentences1,"JOB_1");
        MockAnnopipeJob job2=new MockAnnopipeJob(sentences2,"JOB_2");
        System.out.println(sentences1);
        System.out.println(sentences2);
    }

    private static List<AnnotationSentenceMock> createMockSentences(int i, String person) {
        List<AnnotationSentenceMock> sentenceMocks=new ArrayList<>();
        for (int j = 0; j<animals.length&&(j <i); j++) {
            sentenceMocks.add(new AnnotationSentenceMock(person+" hatte mal einen "+animals[j]+"."));
        }
        return sentenceMocks;
    }
}
