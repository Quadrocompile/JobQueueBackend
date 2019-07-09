package com.quadrocompile.jobqueuebackend.annopipeQueues;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;


//der übergreordnete JobScheduler; er leitet die Jobs zu ihren einzelnen SlaveSchedulern für den jeweiligen Stage (Tokenizer etc) weiter
public class PipelineMasterScheduler implements Runnable {
    private static String[] animals={"Kater","Hund","Kolibri","Alligator","Ameisenbär","Eichenprozessionsspinner","Uhu","Elefant"};

    private final static LinkedBlockingDeque<AnnopipeJob> sumbitQueue = new LinkedBlockingDeque<>();
    private final static List<FutureTask<AnnopipeJob>> jobTasks = new LinkedList<>(); // Do not modify this list outside the scheduler's thread as this could lead to ConcurrentModificationExceptions!

    private final ExecutorService executorService;
    private final ExecutorService watchdog = Executors.newFixedThreadPool(1);
    private final static List<PipelineSlaveScheduler> stageScheduler=new ArrayList<>();

    public PipelineMasterScheduler(){
        // Initialize with a number of worker threads equal to the number of available cpu threads
        this(Runtime.getRuntime().availableProcessors());
    }
    public PipelineMasterScheduler(int threads){
        executorService =  Executors.newFixedThreadPool(threads);

        // run the scheduler in a new thread
        watchdog.submit(this);
    }
    public static void main(String[] args) {


        List<AnnotationSentenceMock> sentences1=createMockSentences(8,"Ich");
        ArrayList<PipelineStage> stages1=new ArrayList<>();
        stages1.add(PipelineStage.TOKENIZER);
        stages1.add(PipelineStage.TREETAGGER);
        List<AnnotationSentenceMock> sentences2=createMockSentences(4,"Peter");
        AnnopipeJob job1=new AnnopipeJob("JOB_1",stages1,sentences1);
        stages1.add(PipelineStage.BERKELEY_PARSER);
        AnnopipeJob job2=new AnnopipeJob("JOB_2",stages1,sentences2);
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

    public static PipelineSlaveScheduler getSchedulerForStage(PipelineStage stage){
        PipelineSlaveScheduler scheduler=null;
        for (int i = 0; i <stageScheduler.size() ; i++) {
            PipelineSlaveScheduler current=stageScheduler.get(i);
            if(current.getPipelineStage()==stage){
                scheduler=current;
            }
        }
        return scheduler;
    }

    @Override
    public void run() {
        try{
            while(!Thread.interrupted()) {
                // Iterate all tasks and check if some of them are finished
                Iterator<FutureTask<AnnopipeJob>> it = jobTasks.iterator();
                while (it.hasNext()) {
                    FutureTask<AnnopipeJob> task = it.next();
                    if (task.isDone()) {
                        AnnopipeJob finishedJob = task.get();

                        // Remove the job from the task list
                        it.remove();

                        // Check the job, if there are games left that need to be analyzed resubmit the job
                        if(!finishedJob.isFinished()) {
                            sumbitQueue.offer(finishedJob);
                        }
                    }
                }

                // Create new tasks for new or rescheduled jobs and submit them to the executor service
                while (sumbitQueue.size() > 0) {
                    AnnopipeJob job = sumbitQueue.poll();
                    if (job != null) {
                        // Create a Future object for the job
                        FutureTask<AnnopipeJob> task = new FutureTask<>(job);

                        // Add Future to our watchlist
                        jobTasks.add(task);

                        // Schedule the task
                        executorService.submit(task);
                    }
                }

                // Pause thread for ten second
                try {
                    //Thread.sleep(10000);
                    Thread.sleep(1000); // shorter duration for debug purposes
                }
                catch (InterruptedException ignored){
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        System.out.println("Shutting down JobScheduler");
    }
}
