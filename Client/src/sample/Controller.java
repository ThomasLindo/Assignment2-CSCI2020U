//this is the client controller class. This class handles the UI as well as sends commands to the server

package sample;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.*;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {
//initalize all UI elements
    @FXML
    private Button DownloadBtn;
    @FXML
    private Button UploadBtn;
    @FXML
    private ListView<String> ServerLst;
    @FXML
    private ListView<String> LocalLst;
    private String SelectedLocalFile;
    private String SelectedServerFile;
    private ObservableList<String> LocalFiles = FXCollections.observableArrayList();
    private ObservableList<String> ServerFiles = FXCollections.observableArrayList();
    //create socket and a printWriter and bufferedReader for input and output from it
    private Socket socket = null;
    private PrintWriter networkOut = null;
    private BufferedReader networkIn = null;
    public static String delimiter = ",";
    //server address and port
    public static String SERVER_ADDRESS = "localhost";
    public static int SERVER_PORT = 16789;
    String line;
    //directory of client files
    File directory = new File("..\\Client\\Local Files");

    public void initialize() {
        try {
            //connect to server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            networkOut = new PrintWriter(socket.getOutputStream(), true);
            networkIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("connected and ready to go");

        //get files in server directory
        networkOut.println("DIR");
        try {
           line = networkIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] data = line.split(delimiter);
        for (String file : data) {
            //populate list with server files
            ServerFiles.add(file);
        }
        File[] contents = directory.listFiles();
        for (File file : contents) {
            //populate list with local files
            LocalFiles.add(file.getName());
        }

//set listeners for both lists so they update when changed
        ServerFiles.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
               ServerLst.refresh();

            }
        });

        LocalFiles.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
               LocalLst.refresh();

            }
        });

        LocalLst.getItems().setAll(this.LocalFiles);
        ServerLst.getItems().setAll(this.ServerFiles);
    }

    public void Download(ActionEvent event) {
        //download selected Server file
        networkOut.println("DOWNLOAD" + " " + ServerLst.getSelectionModel().getSelectedItem());
        String data = "";
        try {
            //get file contents from server
            data = networkIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //create new file in directory
        File outFile = new File(directory.getAbsolutePath(), ServerLst.getSelectionModel().getSelectedItem());
        try {
            if (!outFile.exists()) {
                outFile.createNewFile();

                PrintWriter fileOut = new PrintWriter(outFile);
                //separate data into lines and print to file
                String [] lines = data.split("_");
                for(String line : lines){
                    fileOut.println(line);
                }

                fileOut.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
refresh();
    }

    public void Upload(ActionEvent event) {
        //Upload selected Local File
        networkOut.println("UPLOAD" + " " +LocalLst.getSelectionModel().getSelectedItem());
        File[] contents = directory.listFiles();
        //get selected file
        for (File file : contents) {
            if (file.getName().equals(LocalLst.getSelectionModel().getSelectedItem())) {
                try {

                    FileReader fileReader = new FileReader(file);
                    BufferedReader fileInput = new BufferedReader(fileReader);
                    String data = "";
                    String line;
                    while ((line = fileInput.readLine()) != null) {
                    //get data from file. separate lines with "_"
                        data = data + line + "_";
                    }
                    //send data to server
                    networkOut.println(data);
                    fileInput.close();
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }


        }
        refresh();

    }
    public void refresh(){
        //refresh server and local file lists
        ServerFiles.clear();
        LocalFiles.clear();
        networkOut.println("DIR");
        try {
            line = networkIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] data = line.split(delimiter);
        for (String file : data) {
            ServerFiles.add(file);
        }
        File[] contents = directory.listFiles();
        for (File file : contents) {
            LocalFiles.add(file.getName());
        }
        LocalLst.getItems().setAll(this.LocalFiles);
        ServerLst.getItems().setAll(this.ServerFiles);
    }
}