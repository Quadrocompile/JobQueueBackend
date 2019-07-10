package com.quadrocompile.jobqueuebackend.annopipeQueues;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

// ist zuständig für deine einzelne Stage der Jobs (Tokenizer, TreeTagger etc.)
public class PipelineSlaveScheduler implements Runnable {
    private PipelineStage pipelineStage;
    private final List<FutureTask<StageBoundAnnopipeJob>> jobTasks = new LinkedList<>();
    private final LinkedBlockingDeque<StageBoundAnnopipeJob> sumbitQueue = new LinkedBlockingDeque<>();
    private ExecutorService executorService;
    private final ExecutorService watchdog = Executors.newFixedThreadPool(1);

    public PipelineSlaveScheduler(PipelineStage pipelineStage) {
        // Initialize with a number of worker threads equal to the number of available cpu threads
        this(1);
        System.out.println("Initiazlizing slave scheduler for stage "+pipelineStage.toString());
        this.pipelineStage = pipelineStage;
        System.out.println(pipelineStage.toString()+" initialized.");
    }

    public PipelineStage getPipelineStage() {
        return pipelineStage;
    }

    public void addJob(StageBoundAnnopipeJob newJob){
        System.out.println("Addjob "+newJob.getJobID()+" to stage "+pipelineStage);
        sumbitQueue.offer(newJob);
    }


    public PipelineSlaveScheduler(int threads){
        executorService =  Executors.newFixedThreadPool(threads);
        // run the scheduler in a new thread
        watchdog.submit(this);
    }

    @Override
    public void run() {
        try{
            while(!Thread.interrupted()) {
                // Iterate all tasks and check if some of them are finished
                Iterator<FutureTask<StageBoundAnnopipeJob>> it = jobTasks.iterator();
                while (it.hasNext()) {
                    FutureTask<StageBoundAnnopipeJob> task = it.next();
                    if (task.isDone()) {
                        StageBoundAnnopipeJob finishedJob = task.get();

                        // Remove the job from the task list
                        it.remove();

                        // Check the job, if there are games left that need to be analyzed resubmit the job
                        if(!finishedJob.isFinished()) {
                            System.out.println("Job"+finishedJob.getJobID()+ " is not finished in "+pipelineStage);
                            sumbitQueue.offer(finishedJob);
                        }
                    }
                }

                // Create new tasks for new or rescheduled jobs and submit them to the executor service
                while (sumbitQueue.size() > 0) {
                    StageBoundAnnopipeJob job = sumbitQueue.poll();
                    if (job != null) {
                        // Create a Future object for the job
                        FutureTask<StageBoundAnnopipeJob> task = new FutureTask<>(job);

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
