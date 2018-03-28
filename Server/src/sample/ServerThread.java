//this is the server thread class. this is executed for each client connected and since it is a thread, can run in paraell
//handle input from the client and send the appropriate data
//In my version, cliont DOES NOT disconnect after a command, this was done so a single client can do multiple commands before disconnecting
//and to avoid making multiple connections per client
package sample;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread {
    //create client socket, printWriter, BufferedReader, and directory of server files
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private File Directory = null;


    public ServerThread(Socket socket, File Directory) {
        super();
        //set socket and directory
        this.socket = socket;
        this.Directory = Directory;
        try {
            //set printWriter and bufferedReader to do input and output with the socket.
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("IOEXception while opening a read/write connection");
        }
    }

    public void run() {
        //this loop will happen continously until it ends
        boolean endOfSession = false;
        while (!endOfSession) {
            //get command from the client and determine if the socket should close
            endOfSession = processCommand();
        }
        try {
            System.out.println("disconect");
            socket.close();
        } catch (IOException e) {
            System.out.println("Error during server socket closing");
            e.printStackTrace();
        }
    }

    protected boolean processCommand() {
        //this function determines the command the client sends and any arguments
        String message ="";
        String filename="";
        String command="";
        try {
            //get message from client
            message = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading command from socket.");
            return true;
        }
        if(!message.isEmpty()) {
            //break the message into words. first word is the command, second word is arguments (if any)
            StringTokenizer st = new StringTokenizer(message);
            command = st.nextToken();

            if (st.hasMoreTokens()) {
                filename = message.substring(command.length() + 1, message.length());
            }
        }
        return processCommand(command, filename);
    }

    protected boolean processCommand(String command, String arguments) {
        //this function executes the given command if it exists, and sets if the socket should be closed
        boolean stopRunning = false;
        if (command.equalsIgnoreCase("DIR")) {
            //get names of files in server directory
            File[] contents = Directory.listFiles();
            String listOfFiles = "";
            for (File file : contents) {
                listOfFiles = listOfFiles + file.getName()+",";
            }
            //send file names to client
            out.println(listOfFiles);
            stopRunning = false;

        } else if (command.equalsIgnoreCase("UPLOAD")) {
            //client uploads a file to server
            try {
                //create new file in directory
                File outFile = new File(Directory.getAbsolutePath(), arguments);
                if (!outFile.exists()) {
                    //if file does not exist create new file
                    outFile.createNewFile();
                    //get file contents from client
                    String data = in.readLine();
                    PrintWriter fileOut = new PrintWriter(outFile);
                    //split the message apart using "_" as a delimiter
                    //to get each line
                    String [] lines = data.split("_");
                    for(String line : lines){
                        //print lines to file
                        fileOut.println(line);
                    }
                    fileOut.close();
                }
            } catch (Exception e) {
                System.out.println("Error during server download");
                e.printStackTrace();
            }
            stopRunning = false;


        } else if (command.equalsIgnoreCase("DOWNLOAD")) {
            //client downloads file from server
            File[] contents = Directory.listFiles();
            for (File file : contents) {
                //find correct file
                if (file.getName().equals(arguments)) {
                    try {
                        //create file/bufferedReader to get data from file
                        FileReader fileReader = new FileReader(file);
                        BufferedReader fileInput = new BufferedReader(fileReader);
                        String data="";
                        String line;
                        while ((line = fileInput.readLine()) != null) {
                            //read in each line in the file. separate lines with "_"
                            data = data + line+"_" ;
                        }
                        //send data to client
                        out.println(data);
                        fileInput.close();
                    } catch (Exception e) {
                        System.out.println("Error during server upload");
                        e.printStackTrace();
                    }
                    stopRunning = false;

                }
            }


        }
        return stopRunning;
    }


}
