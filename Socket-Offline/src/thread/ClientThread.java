package thread;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable
{
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 5073;
    private static final int CHUNK_SIZE = 4*1024;

    private Socket clientSocket;
    private File inputFile;
    private Thread th;

    private PrintWriter pw;
    private OutputStream out;
    private BufferedInputStream in;

    public ClientThread(String filename)
    {
        inputFile = new File(filename);
        th = new Thread(this);
        th.start();
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
            pw = new PrintWriter(clientSocket.getOutputStream());
            pw.write("UPLOAD " + inputFile.getName() + "\r\n");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkFileValidity()
    {
        try {
            if(!inputFile.exists()) {
                pw.write("invalid\r\n");
                pw.flush();
                System.out.println("## Given file name is invalid ##");
                pw.close();
                clientSocket.close();
                return false;
            } else {
                pw.write("valid\r\n");
                pw.flush();
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
            pw.close();
            clientSocket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
