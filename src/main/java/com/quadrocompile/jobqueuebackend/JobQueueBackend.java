package com.quadrocompile.jobqueuebackend;

import com.quadrocompile.jobqueuebackend.backend.BackendServer;
import com.quadrocompile.jobqueuebackend.model.Game;
import com.quadrocompile.jobqueuebackend.model.Job;
import com.quadrocompile.jobqueuebackend.scheduler.JobScheduler;

import java.util.ArrayList;
import java.util.List;

public class JobQueueBackend {

    public static void main(String[] args) throws Exception{
        /* for testing the scheduler */

        taskTest();
        System.exit(0);


        /* for running the jetty server */
        runServer();
    }

    private static JobScheduler scheduler;
    public static JobScheduler getScheduler(){
        return scheduler;
    }
    private static void runServer() throws Exception{
        scheduler = new JobScheduler();
        BackendServer.getInstance().startServer();
    }

    private static void taskTest() throws Exception{
        List<Game> gameList;
        Job job;

        // Start a test scheduler with 2 threads. Leave the thread parameter empty to use default number of threads
        JobScheduler scheduler = new JobScheduler(2);

        // Testjob #1
        gameList = new ArrayList<>();
        gameList.add(new Game("Job#1-Game#1/5"));
        gameList.add(new Game("Job#1-Game#2/5"));
        gameList.add(new Game("Job#1-Game#3/5"));
        gameList.add(new Game("Job#1-Game#4/5"));
        gameList.add(new Game("Job#1-Game#5/5"));
        job = new Job("Job#1", gameList);
        scheduler.addJob(job);

        // Testjob #2
        gameList = new ArrayList<>();
        gameList.add(new Game("Job#2-Game#1/3"));
        gameList.add(new Game("Job#2-Game#2/3"));
        gameList.add(new Game("Job#2-Game#3/3"));
        job = new Job("Job#2", gameList);
        scheduler.addJob(job);

        // Wait before submitting the next jobs
        Thread.sleep(100*60*2);

        // Testjob #3
        gameList = new ArrayList<>();
        gameList.add(new Game("Job#3-Game#1/1"));
        job = new Job("Job#3", gameList);
        scheduler.addJob(job);

        // Testjob #4
        gameList = new ArrayList<>();
        gameList.add(new Game("Job#4-Game#1/1"));
        job = new Job("Job#4", gameList);
        scheduler.addJob(job);

        while(true){
            // Check every second if all jobs are finished to exit this function. As a backend service the scheduler should never be terminated obviously...
            Thread.sleep(1000);
            if(scheduler.getScheduledJobs()==0){
                System.out.println("All jobs finished!");
                boolean result = scheduler.shutdownAndAwaitTermination(10000);
                return;
            }
        }

    }
}
