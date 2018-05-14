package com.quadrocompile.jobqueuebackend.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;

public class Job implements Callable<Job> {

    private final LinkedBlockingDeque<Game> queuedGames = new LinkedBlockingDeque<>();
    private final LinkedBlockingDeque<Game> finishedGames = new LinkedBlockingDeque<>();

    private String jobID;
    private int batchSize; // How many games will be analyzed per call

    public Job(String jobID, Collection<Game> games){
        this(jobID, games, 1);
    }

    public Job(String jobID, Collection<Game> games, int batchSize){
        this.jobID = jobID;
        this.queuedGames.addAll(games);
        // Override the default batch size. Maybe prioritize "premium" users by analyzing more games per turn
        this.batchSize = batchSize;
    }

    public String getID(){
        return jobID;
    }

    public List<Game> getQueuedGames(){
        return new ArrayList<>(queuedGames);
    }

    public List<Game> getFinishedGames(){
        return new ArrayList<>(finishedGames);
    }

    public void addGame(Game game){
        queuedGames.add(game);
    }

    public boolean isFinished(){
        return queuedGames.size()==0;
    }

    public int remainingGames(){
        return queuedGames.size();
    }

    @Override
    public Job call(){
        int analyzedGames = 0;

        while(analyzedGames < batchSize && queuedGames.size() > 0){
            ++analyzedGames;
            Game currentGame = queuedGames.poll();

            if(currentGame != null) {
                System.out.println("Analyze " + currentGame.toString());
                try {
                    if (currentGame.analyze()) {
                        finishedGames.add(currentGame);
                        System.out.println("Finished analyzing " + currentGame.toString());
                    } else {
                        System.out.println("Cannot analyze " + currentGame.toString());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

        // Return reference to this job
        return this;
    }

    public String toString(){
        return "Job[" + jobID + "]";
    }
}
