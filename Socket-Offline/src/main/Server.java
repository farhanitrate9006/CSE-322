package main;

import thread.ServerThread;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class Server
{
    private static final String ROOT_PATH = "root";
    private static final String UPLOADED_PATH = "uploaded";
    private static final String LOG_PATH = "log";
    private static final int SERVER_PORT = 5073;

    private static void resetFolder(String PATH)
    {
        File logDir = new File(PATH);
        if(logDir.exists())
        {
            String[] entries = logDir.list();
            for(String entry: entries)
            {
                File deleteLog = new File(logDir.getPath(), entry);
                deleteLog.delete();
            }
            logDir.delete();
        }
        logDir.mkdir();
    }

    public static void main(String[] args) throws IOException
    {
        resetFolder(LOG_PATH);
        resetFolder(UPLOADED_PATH);

        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("### Waiting for connection on port no: " + SERVER_PORT + " ###");

        /* starting accepting http requests */
        while(true)
            new ServerThread(serverSocket.accept(), ROOT_PATH, UPLOADED_PATH, LOG_PATH);
    }
}
