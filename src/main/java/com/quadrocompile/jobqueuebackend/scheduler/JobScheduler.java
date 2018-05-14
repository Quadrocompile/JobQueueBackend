package com.quadrocompile.jobqueuebackend.scheduler;

import com.quadrocompile.jobqueuebackend.model.Job;

import java.util.*;
import java.util.concurrent.*;

public class JobScheduler implements Runnable {
    private final Map<String, Job> jobMap = new ConcurrentHashMap<>();

    private final LinkedBlockingDeque<Job> sumbitQueue = new LinkedBlockingDeque<>();

    private final List<FutureTask<Job>> jobTasks = new LinkedList<>(); // Do not modify this list outside the scheduler's thread as this could lead to ConcurrentModificationExceptions!
    private final ExecutorService executorService;

    private final ExecutorService watchdog = Executors.newFixedThreadPool(1);

    public JobScheduler(){
        // Initialize with a number of worker threads equal to the number of available cpu threads
        this(Runtime.getRuntime().availableProcessors());
    }
    public JobScheduler(int threads){
        executorService =  Executors.newFixedThreadPool(threads);

        // run the scheduler in a new thread
        watchdog.submit(this);
    }

    public void addJob(Job job){
        // Store reference to the job
        jobMap.put(job.getID(), job);
        sumbitQueue.offer(job);
        System.out.println("SCHEDULER - Added job " + job.toString());
    }

    public boolean isJobFinished(String id){
        return jobMap.get(id).isFinished();
    }

    public int getRemainingGamesForJob(String id){
        return jobMap.get(id).remainingGames();
    }

    public int getScheduledJobs(){
        return jobTasks.size();
    }

    // This function maintains our task queue. It checks if a job has been processed by a thread and may resubmit a job to the executor service if there are still games left
    public void run(){
        try{
            while(!Thread.interrupted()) {
                // Iterate all tasks and check if some of them are finished
                Iterator<FutureTask<Job>> it = jobTasks.iterator();
                while (it.hasNext()) {
                    FutureTask<Job> task = it.next();
                    if (task.isDone()) {
                        Job finishedJob = task.get();

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
                    Job job = sumbitQueue.poll();
                    if (job != null) {
                        // Create a Future object for the job
                        FutureTask<Job> task = new FutureTask<>(job);

                        // Add Future to our watchlist
                        jobTasks.add(task);

                        // Schedule the task
                        executorService.submit(task);
                    }
                }

                //System.out.println("SCHEDULER - Queued tasks: " + jobTasks.size());

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

    public boolean shutdownAndAwaitTermination(long timeout){
        // Do not start new jobs
        executorService.shutdown();
        watchdog.shutdownNow();

        try {
            // wait for [timeout] seconds for tasks to finish
            if(!executorService.awaitTermination(timeout, TimeUnit.SECONDS)){
                // Force shutdown active tasks. But you'd need to check  Thread.currentThread().isInterrupted()  in your analysis code to react to it. Otherwise this does nothing!
                executorService.shutdownNow();
                // Wait for [timeout] seconds for the tasks to respond to your interrupt signal
                if(!executorService.awaitTermination(timeout, TimeUnit.SECONDS)){
                    System.err.println("Failed to shutdown executor. Some tasks are still active!");
                    return false;
                }
            }
        }
        catch (InterruptedException intex){
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }

        return true;
    }
}
