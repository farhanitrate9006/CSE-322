package thread;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable
{
    private final String SERVER_ADDRESS = "127.0.0.1";
    private final int SERVER_PORT = 5073;
    private final int CHUNK_SIZE = 4*1024;

    private Socket clientSocket;
    private File inputFile;
    private Thread th;

    private PrintWriter pr;
    private OutputStream out;
    private BufferedInputStream in;

    public ClientThread(String filename)
    {
        inputFile = new File(filename);
        th = new Thread(this);
        th.start();
    }

    private void openSocket()
    {
        try {
            clientSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeUploadRequest()
    {
        try {
            pr = new PrintWriter(clientSocket.getOutputStream());
            pr.write("UPLOAD " + inputFile.getName() + "\r\n");
            pr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkFileValidity()
    {
        try {
            if(!inputFile.exists()) {
                pr.write("invalid\r\n");
                pr.flush();
                System.out.println("## Given file name is invalid ##");

                pr.close();
                clientSocket.close();
                return false;
            } else {
                pr.write("valid\r\n");
                pr.flush();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void fileUpload()
    {
        int count;
        byte[] buffer = new byte[CHUNK_SIZE];

        try {
            out = clientSocket.getOutputStream();
            in = new BufferedInputStream(new FileInputStream(inputFile));

            while((count=in.read(buffer)) > 0)
            {
                out.write(buffer, 0, count);
                out.flush();
            }

            in.close();
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void closeStreams()
    {
        try {
            pr.close();
            clientSocket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        openSocket();
        writeUploadRequest();
        if(!checkFileValidity())
            return;
        fileUpload();
        closeStreams();
    }
}
