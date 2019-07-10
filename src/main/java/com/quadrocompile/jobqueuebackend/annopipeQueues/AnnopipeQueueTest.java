package com.quadrocompile.jobqueuebackend.annopipeQueues;

import java.util.ArrayList;
import java.util.List;

public class AnnopipeQueueTest {
    private  static PipelineMasterScheduler masterScheduler;
    private static String[] animals={"Kater","Hund","Kolibri","Alligator","Ameisenbär","Eichenprozessionsspinner","Uhu","Elefant"};
    public static void main(String[] args) throws InterruptedException {
        masterScheduler=new PipelineMasterScheduler();

        ArrayList<PipelineStage> stages1=new ArrayList<>();
        stages1.add(PipelineStage.TOKENIZER);
        stages1.add(PipelineStage.TREETAGGER);
        AnnopipeJob job1=new AnnopipeJob("JOB_1",stages1,createMockSentences(8,"Ich"));
        stages1.add(PipelineStage.BERKELEY_PARSER);
        AnnopipeJob job2=new AnnopipeJob("JOB_2",stages1,createMockSentences(4,"Peter"));
        AnnopipeJob job3=new AnnopipeJob("JOB_3",stages1,createMockSentences(7,"Hans"));
        AnnopipeJob job4=new AnnopipeJob("JOB_4",stages1,createMockSentences(6,"Susi"));
        masterScheduler.addJob(job1);
        Thread.sleep(200);
        masterScheduler.addJob(job2);
        Thread.sleep(200);
        masterScheduler.addJob(job3);
        Thread.sleep(200);
        masterScheduler.addJob(job4);
        Thread.sleep(10000);
        System.out.println(job1);
        System.out.println(job2);
        System.out.println(job3);
        System.out.println(job4);

    }
    private static List<AnnotationSentenceMock> createMockSentences(int i, String person) {
        List<AnnotationSentenceMock> sentenceMocks=new ArrayList<>();
        for (int j = 0; j<animals.length&&(j <i); j++) {
            sentenceMocks.add(new AnnotationSentenceMock(person+" hatte mal einen "+animals[j]+"."));
        }
        return sentenceMocks;
    }
}
