package com.quadrocompile.jobqueuebackend.model;

public class Game {

    private final String id;

    public Game(String id){
        // Initialize
        this.id = id;
    }

    public boolean analyze() throws Exception{

        /* Simulate the analysis of this game by suspending the thread for 90 sec */
        //Thread.sleep(1000*90);
        Thread.sleep(100*90); // shorter duration for demonstration purposes

        return true;
    }

    public String toString(){
        return "Game[" + id + "]";
    }
}
