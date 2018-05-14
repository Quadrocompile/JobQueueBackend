package com.quadrocompile.jobqueuebackend.backend;

import com.quadrocompile.jobqueuebackend.backend.servlets.JobServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.LinkedBlockingQueue;

public class BackendServer {

    private final Server SERVER;
    private static final int PORT = 8080;

    private BackendServer() throws Exception {
        int maxthreads=20;
        int minthreads=4;
        int timeout=60000;
        int capacity=5000;
        int acceptors=-1;
        int selectors=-1;
        SERVER = new Server(new QueuedThreadPool(maxthreads, minthreads, timeout, new LinkedBlockingQueue<>(capacity)));

        ServerConnector serverConnector = new ServerConnector(SERVER, acceptors, selectors);
        serverConnector.setPort(PORT);
        SERVER.setConnectors(new Connector[]{serverConnector});

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        SERVER.setHandler(context);

        // Add the job servlet
        context.addServlet(JobServlet.class, "/webapp/jobs");

        // Catchall. This could direct to the index page
        context.addServlet(JobServlet.class,"/");
    }

    public void startServer() throws Exception{
        SERVER.start();
        SERVER.join();
    }

    public void shutdownServer() throws Exception{
        SERVER.stop();
    }

    private static BackendServer instance;
    public static BackendServer getInstance() throws Exception{
        if(instance == null){
            synchronized(BackendServer.class){
                instance = new BackendServer();
            }
        }
        return instance;
    }
}
