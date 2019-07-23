package com.quadrocompile.jobqueuebackend.annopipeQueues;

import java.util.ArrayList;
import java.util.List;

public class AnnopipeQueueTest {
    private  static PipelineMasterScheduler masterScheduler;
    private static String[] animals={"Kater","Hund","Kolibri","Alligator","Ameisenbär","Eichenprozessionsspinner","Uhu","Elefant"};
    public static void main(String[] args) throws InterruptedException {
        //als erstes wird ein master-scheduler initialisiert
        masterScheduler=new PipelineMasterScheduler();

        //liste von Stages (Tokenizer etc.), die für die beispieljobs gebraucht wird
        ArrayList<PipelineStage> stages1=new ArrayList<>();
        stages1.add(PipelineStage.TOKENIZER);
        stages1.add(PipelineStage.TREETAGGER);

        AnnopipeJob job1=new AnnopipeJob("JOB_1",stages1,createMockSentences(40,"Ich"));
        stages1.add(PipelineStage.BERKELEY_PARSER);
        //übergebe dem masterscheduler den job, den rest macht er mit seinen slaves aus!
        masterScheduler.addJob(job1);
        Thread.sleep(100);
        AnnopipeJob job2=new AnnopipeJob("JOB_2",stages1,createMockSentences(37,"Peter"));
        masterScheduler.addJob(job2);
        Thread.sleep(100);
        AnnopipeJob job3=new AnnopipeJob("JOB_3",stages1,createMockSentences(7,"Hans"));
        masterScheduler.addJob(job3);
        Thread.sleep(200);
        AnnopipeJob job4=new AnnopipeJob("JOB_4",stages1,createMockSentences(25,"Susi"));
        masterScheduler.addJob(job4);
        Thread.sleep(20000);

        List<AnnopipeJob> finishedSentences=PipelineMasterScheduler.getFinishedJobs();
        for (AnnopipeJob ajob:finishedSentences
             ) {
            System.out.println("Job '"+ajob.getJobID()+"': "+ajob);
        }

    }

    //erstelle i +1 dumme Besipielsätze
    private static List<AnnotationSentenceMock> createMockSentences(int i, String person) {
        List<AnnotationSentenceMock> sentenceMocks=new ArrayList<>();
        for (int j = 0; j< i; j++) {
            sentenceMocks.add(new AnnotationSentenceMock(person+" hatte mal einen "+animals[j%animals.length]+j+"."));
        }
        return sentenceMocks;
    }
}
