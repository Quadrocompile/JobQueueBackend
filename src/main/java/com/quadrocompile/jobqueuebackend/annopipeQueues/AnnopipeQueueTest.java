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
        AnnopipeJob job1=new AnnopipeJob("JOB_1",stages1,createMockSentences(33,"Ich"));
        stages1.add(PipelineStage.BERKELEY_PARSER);
        AnnopipeJob job2=new AnnopipeJob("JOB_2",stages1,createMockSentences(15,"Peter"));
        AnnopipeJob job3=new AnnopipeJob("JOB_3",stages1,createMockSentences(7,"Hans"));
        AnnopipeJob job4=new AnnopipeJob("JOB_4",stages1,createMockSentences(37,"Susi"));
        masterScheduler.addJob(job1);
        //Thread.sleep(200);
        masterScheduler.addJob(job2);
        //Thread.sleep(200);
        masterScheduler.addJob(job3);
        //Thread.sleep(200);
        masterScheduler.addJob(job4);
        Thread.sleep(20000);
        List<AnnopipeJob> finishedSentences=PipelineMasterScheduler.getFinishedJobs();
        for (AnnopipeJob ajob:finishedSentences
             ) {
            System.out.println("Job '"+ajob.getJobID()+"': "+ajob);
        }

    }
    private static List<AnnotationSentenceMock> createMockSentences(int i, String person) {
        List<AnnotationSentenceMock> sentenceMocks=new ArrayList<>();
        for (int j = 0; j< i; j++) {
            sentenceMocks.add(new AnnotationSentenceMock(person+" hatte mal einen "+animals[j%animals.length]+j+"."));
        }
        return sentenceMocks;
    }
}
