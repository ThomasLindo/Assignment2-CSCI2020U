//this is the main server, you'll need to run this before you run a client and creates a server thread for each client that joins.
//handles any new incoming clients by creating a server thread for each
package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main{
    //create client socket, server socket, and an array of server threads
    private Socket clientSocket           = null;
    private ServerSocket serverSocket     = null;
    private ServerThread[] threads    = null;
    private int numClients                = 0;
    //directory for the server files
    File directory = new File("..\\Server\\Server Files");
    //port and max clients
    public static int SERVER_PORT = 16789;
    public static int MAX_CLIENTS = 25;

    public Main(){
        try {
            //create the server socket and initalize the threads array
            serverSocket = new ServerSocket(SERVER_PORT);
            threads = new ServerThread[MAX_CLIENTS];
            //while running, accept any new client that tries to connect and create a server thread for it
            while(true) {
                clientSocket = serverSocket.accept();
                System.out.println("Client #"+(numClients+1)+" connected.");
                threads[numClients] = new ServerThread(clientSocket, directory);
                threads[numClients].start();
                numClients++;
            }
        } catch (IOException e) {
            System.err.println("IOException while creating server connection");
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
    }

}
