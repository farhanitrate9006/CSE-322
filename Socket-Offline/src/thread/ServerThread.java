package thread;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerThread implements Runnable
{
    private final String separator = File.separator + File.separator;
    private final Socket serverSocket;
    private static String ROOT_PATH = null;
    private static String UPLOADED_PATH = null;
    private static String LOG_PATH = null;
    private static int request_no = 0;

    private BufferedReader br;
    private PrintWriter pw, fw;
    private String html, httpRequest, httpResponse, requestedPath;
    private File requestedFile;
    // private StringBuilder sb;

    public ServerThread(Socket socket, String ROOT_PATH, String UPLOADED_PATH, String LOG_PATH)
    {
        this.serverSocket = socket;
        ServerThread.ROOT_PATH = ROOT_PATH;
        ServerThread.UPLOADED_PATH = UPLOADED_PATH;
        ServerThread.LOG_PATH = LOG_PATH;
        new Thread(this).start();
    }

    @Override
    public void run()
    {
        //sb = new StringBuilder();
        openStreams();
        receiveRequest();
        if(httpRequest == null)
            closeConnection();
        else if(httpRequest.startsWith("GET") && httpRequest.endsWith("HTTP/1.1")) // GET /... HTTP/1.1
        {
            openWriters();
            fw.println("http request from client:\n" + httpRequest + "\n\nhttp response from server:");
            requestedPath = extractPath();

            if(requestedPath.equals(""))
            {
                handleRoot();
                closeAllConnection();
                return;
            }

            requestedFile = new File(requestedPath.replace("%20", " ").replace("/", separator));
            if(requestedFile.exists())
            {
                httpResponse = "HTTP/1.1 200 OK\r\n";
                httpResponse += "Server: Java HTTP Server: 1.0\r\n";
                httpResponse += "Date: " + new Date() + "\r\n";

                if(requestedFile.isDirectory())
                    handleDirectory();
                else if(requestedFile.isFile())
                {
                    int temp = requestedFile.getName().lastIndexOf('.');
                    String fileExtension = requestedFile.getName().substring(temp+1);
                    if(fileExtension.equals("png") || fileExtension.equals("jpg"))
                        httpResponse += "Content-Type: image/jpg\r\n";
                    else if(fileExtension.equals("txt"))
                        httpResponse += "Content-Type: text/html\r\n";
                    else
                        httpResponse += "Content-Type: application/x-force-download\r\n";
                    handleFile();
                }
            }
            else
                handleNotFound();

            closeAllConnection();
        }
    }

    private void handleFile()
    {
        httpResponse += "Content-Length: " + requestedFile.length() + "\r\n";
        fw.println(httpResponse);
        pw.write(httpResponse);
        pw.write("\r\n");
        pw.flush();

        int count;
        byte[] buffer = new byte[1024];

        try {
            OutputStream out = serverSocket.getOutputStream();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(requestedFile));

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

    private void handleDirectory()
    {
        httpResponse += "Content-Type: text/html\r\n";
        html = "<html>\n<body>\n";

        File[] subFiles = requestedFile.listFiles();
        assert subFiles != null;
        if(subFiles.length == 0)
            html += "<h1> Directory is currently empty </h1>\n";
        else
        {
            for (File subFile : subFiles) {
                if (subFile.isDirectory()) {
                    html += "<em> <a href=\"/" + requestedPath + "/" +
                            subFile.getName() + "\">" + subFile.getName() + "</a> </em> <br>\n";
                } else if (subFile.isFile()) {
                    html += "<a href=\"/" + requestedPath + "/" +
                            subFile.getName() + "\">" + subFile.getName() + "</a> <br>\n";
                }

            }
        }

        html += "</body>\n</html>";
        httpResponse += "Content-Length: " + html.length() + "\r\n";
        fw.println(httpResponse);
        pw.write(httpResponse);
        pw.write("\r\n");
        pw.write(html);
        pw.flush();
    }

    public void handleRoot()
    {
        html = "<html>\n<body>\n" +
                "<em> <a href=\"" + ROOT_PATH + "\"> root </a> </em> <br>\n" +
                "<em> <a href=\"" + UPLOADED_PATH + "\"> uploaded </a> </em> <br>\n" +
                "</body>\n</html>";
        httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Server: Java HTTP Server 1.0\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + html.length() + "\r\n";
        fw.write(httpResponse);
        pw.write(httpResponse);
        pw.write("\r\n");
        pw.write(html);
        pw.flush();
    }

    private void handleNotFound()
    {
        html = "<html>\n<body>\n";
        html += "<h1> Status 404: Page Not Found </h1>\n";
        html += "</body>\n</html>";

        httpResponse = "HTTP/1.1 404 NOT FOUND\r\n";
        httpResponse += "Server: Java HTTP Server: 1.0\r\n";
        httpResponse += "Date: " + new Date() + "\r\n";
        httpResponse += "Content-Length: " + html.length() + "\r\n";

        fw.println(httpResponse);
        pw.write(httpResponse);
        pw.write("\r\n");
        pw.write(html);
        pw.flush();
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
            //System.out.println(httpRequest);
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

    private void closeAllConnection()
    {
        closeConnection();
        pw.close();
        fw.close();
    }

    private void openWriters()
    {
        try {
            pw = new PrintWriter(serverSocket.getOutputStream());
            fw = new PrintWriter(LOG_PATH + separator + "log-" + (++request_no) + ".txt");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String extractPath()
    {
        // GET /... HTTP/1.1
        // So, unnecessary part of beginning is "GET /" --- 5 characters and ending is " HTTP/1.1" --- 9 characters
        return httpRequest.substring(5, httpRequest.length()-9);
    }
}
