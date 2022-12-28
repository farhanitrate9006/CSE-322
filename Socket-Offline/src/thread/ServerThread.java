package thread;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerThread implements Runnable
{
    private String separator = File.separator + File.separator;
    private Socket serverSocket;
    private String root;
    private String log;
    private final int SERVER_PORT;
    private static int request_no = 0;
    private Thread th;

    private BufferedReader br;
    private PrintWriter pw, fw;
    private String html, httpRequest, httpResponse, requestedPath;
    private File requestedFile;
    private StringBuilder sb;

    public ServerThread(Socket socket, String root, String log, int SERVER_PORT)
    {
        this.serverSocket = socket;
        this.root = root;
        this.log = log;
        this.SERVER_PORT = SERVER_PORT;
        th = new Thread(this);
        th.start();
    }

    @Override
    public void run()
    {
        sb = new StringBuilder();
        openStreams();
        receiveRequest();
        if(httpRequest == null)
            closeConnection();
        else if(httpRequest.startsWith("GET") && httpRequest.endsWith("HTTP/1.1")) // GET /... HTTP/1.1
        {
            openWriters();
            fw.println("http request from client:\n" + httpRequest);
            requestedPath = extractPath();
            if(requestedPath.equals(""))
                requestedFile = new File("root");
            else
            {
                requestedPath = requestedPath.replaceAll("%20", " ");
                requestedFile = new File(requestedPath);
            }

            if(requestedFile.exists())
            {
                httpResponse = "HTTP/1.1 200 OK\r\n";
                httpResponse += "Server: Java HTTP Server: 1.0\r\n";
                httpResponse += "Date: " + new Date() + "\r\n";
                html = "<html>\n<body>";

                if(requestedFile.isDirectory())
                {
                    httpResponse += "Content-Type: text/html\r\n";

                    File[] subFiles = requestedFile.listFiles();

                    for(int i=0; i<subFiles.length; i++)
                    {
                        if(subFiles[i].isDirectory())
                        {
                            html += "<i> <a href=\"/" + requestedPath + "/" +
                                    subFiles[i] + "\">" + subFiles[i] + "</a> </i> <br>\n";
                        }
                        else if(subFiles[i].isFile())
                        {
                            html += "<a href=\"/" + requestedPath + "/" +
                                    subFiles[i] + "\">" + subFiles[i] + "</a> <br>\n";
                        }

                    }

                    httpResponse += "Content-Length: " + sb.toString().length() + "\r\n";
                    /* NOTICE: log file will contain request line, response line and response header; not response body */
                    fw.println(httpResponse);

                    pw.write(httpResponse);
                    pw.write("\r\n");
                    pw.write(sb.toString());
                    pw.flush();
                }

                html = "</body>\n</html>";
                sb.append(html);
                sb.append('\n');
            }
        }
    }

    private void openStreams()
    {
        try {
            br = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveRequest()
    {
        try {
            httpRequest = br.readLine();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection()
    {
        try {
            br.close();
            serverSocket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void openWriters()
    {
        try {
            pw = new PrintWriter(serverSocket.getOutputStream());
            fw = new PrintWriter(log + separator + "log-" + (++request_no) + ".txt");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String extractPath()
    {
        // GET /... HTTP/1.1
        // So, unnecessary part of beginning is "GET /" --- 5 characters and ending is " HTTP/1.1" --- 9 characters
        return httpRequest.substring(5, httpRequest.length()-9).replaceAll("/", separator);
    }
}
