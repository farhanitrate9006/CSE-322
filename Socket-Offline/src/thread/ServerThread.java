package thread;

import java.io.*;
import java.net.Socket;
import java.util.Date;

enum ValidExtension
{
    txt,
    jpg,
    png,
    pdf,
    mp4
}

public class ServerThread implements Runnable
{
    private final String SEPARATOR = File.separator + File.separator; // SEPARATOR will vary in different OS
    private final Socket serverSocket;

    // different paths for different folders
    private static String ROOT_PATH = null;
    private static String UPLOADED_PATH = null;
    private static String LOG_PATH = null;

    private static int request_no = 0; // to keep count of http request-response log files
    private static final int CHUNK_SIZE = 4*1024; // will be used for downloading/uploading

    private BufferedReader br;
    private PrintWriter pw, fw;
    private String html, httpRequest, httpResponse, requestedPath; // necessary strings
    private File requestedFile;

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
        openStreams();
        receiveRequest();
        if(httpRequest == null)
            closeConnection();
        else if(httpRequest.startsWith("UPLOAD"))
        {
            if(!checkFormat())
            {
                closeConnection();
                return;
            }
            handleUpload();
            closeConnection();
            // System.out.println("upload completed");
        }
        else if(httpRequest.startsWith("GET") && httpRequest.endsWith("HTTP/1.1")) // Example: "GET /... HTTP/1.1"
        {
            openWriters();
            fw.println("http request from client:\n" + httpRequest + "\n\nhttp response from server:");
            requestedPath = extractPath(); // extracting only the necessary portions

            if(requestedPath.equals("")) // requesting for root folder ("GET / HTTP/1.1")
            {
                handleRoot();
                closeAllConnection();
                return;
            }

            // path transformation (to be consistent with local file system naming)
            requestedFile = new File(requestedPath.replace("%20", " ").replace("/", SEPARATOR));
            if(!requestedFile.exists())
                handleNotFound();
            else
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

            closeAllConnection();
        }
        else
            closeConnection();
    }

    private boolean checkFormat()
    {
        // Example request: "UPLOAD ..."
        // So, unnecessary part is "UPLOAD " -> 7 characters ...
        String fileName = httpRequest.substring(7);
        int temp = fileName.lastIndexOf('.');
        String fileExtension = fileName.substring(temp+1);

        try {
            pw = new PrintWriter(serverSocket.getOutputStream());
        } catch(IOException e) {
            e.printStackTrace();
        }

        for(ValidExtension extension : ValidExtension.values())
        {
            if(extension.name().equals(fileExtension)) // valid extension
            {
                pw.println("yes");
                pw.println("\r\n");
                pw.flush();
                return true;
            }
        }

        // invalid extension
        pw.println("no");
        pw.println("\r\n");
        pw.flush();
        return false;
    }

    private void handleUpload()
    {
        // Example request: "UPLOAD ..."
        // So, unnecessary part is "UPLOAD " -> 7 characters ...
        String fileName = httpRequest.substring(7);
        requestedFile = new File(UPLOADED_PATH + "\\" + fileName);
        byte[] buffer = new byte[CHUNK_SIZE];
        int count;

        try {
            FileOutputStream fos = new FileOutputStream(requestedFile);
            InputStream in = serverSocket.getInputStream();

            while((count=in.read(buffer)) > 0)
                fos.write(buffer, 0, count);

            in.close();
            fos.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFile() // file will be sent chunk-by-chunk
    {
        httpResponse += "Content-Length: " + requestedFile.length() + "\r\n";
        fw.println(httpResponse);
        pw.write(httpResponse);
        pw.write("\r\n");
        pw.flush();

        int count;
        byte[] buffer = new byte[CHUNK_SIZE];

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

    private void handleDirectory() // sub-file or sub-folder list will be rendered as html links
    {
        httpResponse += "Content-Type: text/html\r\n";
        html = "<html>\n<body>\n";

        File[] subFiles = requestedFile.listFiles();
        assert subFiles != null; // intellij suggested so, hence added this line
        if(subFiles.length == 0)
            html += "<h1> Directory is currently empty </h1>\n";
        else
        {
            for (File subFile : subFiles) {
                if (subFile.isDirectory()) { // italic font (<em>) for sub-directory
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

        System.out.println("### Requested path: /" + requestedPath.replace("%20", " "));
        System.out.println("### Page 404: Path Not Found");
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
            fw = new PrintWriter(LOG_PATH + SEPARATOR + "log-" + (++request_no) + ".txt");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String extractPath()
    {
        // Example request: "GET /... HTTP/1.1"
        // So, unnecessary part of beginning is "GET /" -> 5 characters ...
        // AND ending is " HTTP/1.1" -> 9 characters
        return httpRequest.substring(5, httpRequest.length()-9);
    }
}
