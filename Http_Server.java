package Http_Server_Library;

import java.net.*;
import java.io.*;
import java.net.Socket;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.*;
import java.util.Scanner;
import java.util.Vector;

public class Http_Server
{

    // initialize socket
    public static void initializeServerSocket(int portNumber, String directory, boolean verbose) // ideally test with 8080, and IP will be localhotst: 127.0.0.1
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Server Instantiated ...");

            while(true)
            {
                Socket clientSocket = serverSocket.accept();

                // call start ClientSocket
                start(clientSocket, directory, verbose);
            }
        }
        catch(IOException e)
        {
            e.getMessage();
        }
    }

    // Start
    public static void start(Socket clientSocket, String directory, boolean verbose)
    {
        try
        {
            // OUTPUT
            OutputStreamWriter os = new OutputStreamWriter(clientSocket.getOutputStream());
            BufferedWriter out = new BufferedWriter(os);

            // INPUT
            InputStreamReader is = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader in = new BufferedReader(is);


            // All variables for Request
            String requestLine = "";
            String requestMethod = "";
            String requestPath = "";
            String requestVersion = "HTTP/1.0";
            String requestHeaders = "";
            String requestEntityBody = "";

            // All variables for Response
            String entireResponse = "";
            String responseVersion = "HTTP/1.0";
            String responseStatusCode = "";
            String responsePhrase = "";
            String responseHeaders = "";
            String responseEntityBody = "";


            // Read and Parse Request
            requestLine = in.readLine();
            String [] splitLine = requestLine.split(" ");

            requestMethod = splitLine[0];
            requestPath = splitLine[1];
            //System.out.println(requestPath);
            //System.out.println(requestPath.length());


            // Get the Headers and the Entity Body
            int c;
            char newChar;
            String requestHeadersBody = "";
            do
            {
                c=in.read();
                newChar = (char)c;

                requestHeadersBody = requestHeadersBody + newChar;
                //System.out.println(response);
            }
            while( (c) != -1  && (in.ready()) );

            // Initialize Headers and the EntityBody
            String[] splitHeadersBody = requestHeadersBody.split("\r\n\r\n");
            requestHeaders = splitHeadersBody[0];

            if(splitHeadersBody.length > 1)
            {
                requestEntityBody = splitHeadersBody[1];
            }


            // SHOW CLIENT REQUEST - When Verbose is true
            if(verbose == true)
            {
                System.out.println("New Client Request: \n" + requestLine + "\n" + requestHeaders + "\n" + requestEntityBody);
            }

            // -------------------- GET --------------------
            if(requestMethod.equals("GET"))
            {
                // 1.0
                // If the requestPath is "/"
                if(requestPath.equals("/"))
                {
                    responseStatusCode = "200";
                    responsePhrase = "OK";
                    responseHeaders = requestHeaders;

                    File folder = new File(directory);
                    File[] listOfFiles = folder.listFiles();

                    for (File currentFile : listOfFiles)
                    {
                        if (currentFile.isFile())
                        {
                            responseEntityBody = responseEntityBody + currentFile.getName().toString() + "\n";
                        }
                    }
                }
                else
                {
                    // 2.0
                    // Check if the file exists (coming from the "requestPath"), and if it does get the data from there
                    File wantedFile = new File(directory + requestPath);
                    if (wantedFile.exists())
                    {
                        try
                        {
                            BufferedReader fr = new BufferedReader(new FileReader(wantedFile));

                            StringBuilder sb = new StringBuilder();
                            String currentLine = fr.readLine();

                            while (currentLine != null)
                            {
                                sb.append(currentLine);
                                sb.append(System.lineSeparator());
                                currentLine = fr.readLine();
                            }
                            responseStatusCode = "200";
                            responsePhrase = "OK";
                            responseEntityBody = sb.toString();
                            responseHeaders = requestHeaders + "\n" + "Content-Length:" + responseEntityBody.length(); // Plus Content-Length header

                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    // If it doesnt then 404 : Not Found
                    else
                    {
                        responseStatusCode = "404";
                        responsePhrase = "Not Found";
                    }
                }

                // Write Response for GET
                out.write(responseVersion + " " + responseStatusCode + " " + responsePhrase + "\r\n" +
                            responseHeaders + "\r\n" + "\r\n" + responseEntityBody);

                out.flush();
                clientSocket.shutdownOutput();
                clientSocket.close();
            }


            // -------------------- POST --------------------
            if(requestMethod.equals("POST"))
            {
                // Cases:
                // 1.0 File exists, write data to file
                // 2.0 File doesnt exist, create file and write data to file

                if ( new File(directory + requestPath).isFile() )
                {
                    File wantedFile = new File(directory + requestPath);
                    BufferedWriter fw = new BufferedWriter(new PrintWriter(new FileWriter(wantedFile, false)));

                    responseStatusCode = "200";
                    responsePhrase = "OK";
                    responseHeaders = requestHeaders;

                    fw.write(requestEntityBody); // write to the file
                    fw.flush();
                    fw.close();
                }
                else
                {
                    File wantedFile = new File(directory + requestPath);
                    BufferedWriter fw = new BufferedWriter(new PrintWriter(new FileWriter(wantedFile, false)));

                    responseStatusCode = "201";
                    responsePhrase = "Created";
                    responseHeaders = requestHeaders;

                    wantedFile.mkdirs();

                    fw.write(requestEntityBody); // write to the file
                    fw.flush();
                    fw.close();
                }

                // Write Response for POST
                out.write(responseVersion + " " + responseStatusCode + " " + responsePhrase + "\r\n" +
                        responseHeaders + "\r\n" + "\r\n" + responseEntityBody);

                out.flush();
                clientSocket.shutdownOutput();
                clientSocket.close();
            }


            /*
            // Write
            out.write("Received the message");
            out.flush();
            clientSocket.shutdownOutput();
            clientSocket.close();
            */
        }
        catch (IOException e)
        {
            e.getMessage();
        }
    }


}// CLASS

