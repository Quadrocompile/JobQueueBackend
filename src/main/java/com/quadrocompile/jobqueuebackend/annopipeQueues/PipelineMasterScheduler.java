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


    private final static LinkedBlockingDeque<AnnopipeJob> sumbitQueue = new LinkedBlockingDeque<>();
    private final static List<FutureTask<AnnopipeJob>> jobTasks = new LinkedList<>(); // Do not modify this list outside the scheduler's thread as this could lead to ConcurrentModificationExceptions!

    private final ExecutorService executorService;
    private final ExecutorService watchdog = Executors.newFixedThreadPool(1);
    private final static List<PipelineSlaveScheduler> stageScheduler=new ArrayList<>();
    private final static List<AnnopipeJob> finishedJobs=new ArrayList<>();

    public PipelineMasterScheduler(){
        // Initialize with a number of worker threads equal to the number of available cpu threads
        this(1);
        System.out.println("initializing master scheduler");
        stageScheduler.add(new PipelineSlaveScheduler(PipelineStage.TOKENIZER));
        stageScheduler.add(new PipelineSlaveScheduler(PipelineStage.TREETAGGER));
        stageScheduler.add(new PipelineSlaveScheduler(PipelineStage.BERKELEY_PARSER));
        System.out.println("Master scheduler fully initialized");
    }
    public PipelineMasterScheduler(int threads){
        executorService =  Executors.newFixedThreadPool(2);

        // run the scheduler in a new thread
        watchdog.submit(this);
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

    public void addJob(AnnopipeJob job){
        sumbitQueue.offer(job);
        System.out.println("SCHEDULER - Added job " + job.toString());
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
                        }else{
                            finishedJobs.add(finishedJob);
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
