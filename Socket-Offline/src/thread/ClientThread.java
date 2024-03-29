package thread;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable
{
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 5173;
    private static final int CHUNK_SIZE = 4*1024;

    private Socket clientSocket;
    private File inputFile;
    private Thread th;

    private PrintWriter pw;
    private OutputStream out;
    private BufferedInputStream in;

    public ClientThread(String filename, String CLIENT_PATH)
    {
        inputFile = new File(CLIENT_PATH + "\\" + filename);
        th = new Thread(this);
        th.start();
    }

    @Override
    public void run()
    {
        if(!inputFile.exists())
        {
            System.out.println("### Given file does not exist");
            return;
        }
        openSocket();
        writeUploadRequest();
        if(!fileStatus())
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

    private boolean fileStatus() // if server says file format is okay, only then fill will be sent
    {
        String fileFormatValid = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // in = new BufferedInputStream(clientSocket.getInputStream());
            fileFormatValid = br.readLine();
            // System.out.println(fileFormatValid);
        } catch(IOException e) {
            e.printStackTrace();
        }
        assert fileFormatValid != null; // intellij suggested so, hence added this line
        if(fileFormatValid.equals("yes"))
            return true;
        System.out.println("### File format is not valid");
        return false;
    }

    private void fileUpload()
    {
        int count;
        byte[] buffer = new byte[CHUNK_SIZE];
        // System.out.println(inputFile.length());

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
