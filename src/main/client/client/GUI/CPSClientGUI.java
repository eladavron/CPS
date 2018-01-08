package client.GUI;

import client.ClientController;
import entity.Message;
import entity.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * The main GUI application
 * @author Elad Avron
 */
public class CPSClientGUI extends Application{

    private static ClientController _client;

    private static AnchorPane _pageRoot;
    private static Label _lblStatus;

    private static HashMap<Long, LinkedList<Message>> _incomingMessages = new HashMap<Long, LinkedList<Message>>();

    private static User _currentUser;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL rootURL = getClass().getResource("GUIRoot.fxml");
        AnchorPane rootPane = FXMLLoader.load(rootURL);
        Scene scene = new Scene(rootPane);

        _pageRoot = (AnchorPane) scene.lookup("#pageRoot");
        _lblStatus = (Label) scene.lookup("#lblStatus");
        changeGUI("LoginScreen.fxml");

        primaryStage.setScene(scene);
        primaryStage.setMinHeight(rootPane.getPrefHeight());
        primaryStage.setMinWidth(rootPane.getPrefWidth());
        primaryStage.show();
    }

    /**
     * Replaces the inner gui page with the supplied filename.
     * @param filename a local fxml filename to load.
     * @throws IOException If the file doesn't exist.
     */
    public static void changeGUI(String filename) {
        try {
            URL guiURL = CPSClientGUI.class.getResource(filename);
            Node guiRoot = FXMLLoader.load(guiURL);
            _pageRoot.getChildren().removeAll();
            _pageRoot.getChildren().add(guiRoot);
            AnchorPane.setTopAnchor(guiRoot, 0.0);
            AnchorPane.setBottomAnchor(guiRoot, 0.0);
            AnchorPane.setLeftAnchor(guiRoot, 0.0);
            AnchorPane.setRightAnchor(guiRoot, 0.0);
        } catch (IOException io)
        {
            //TODO: Handle GUI failures.
        }
    }

    public static void sendToServer(Message message)
    {
        try {
            //TODO: Verify JSON string
            _client.sendToServer(message.toJson());
        } catch (IOException e) {
            setStatus("An error occurred communicating with the server!", Color.RED);
        }
    }

    public static void addMessageToQueue(Message message)
    {
        LinkedList<Message> list;
        long sid = message.getSID();
        if (!_incomingMessages.containsKey(sid)) //No queue exists for this sid
        {
            list = new LinkedList<Message>();
        }
        else { //This SID has a queue, just have to update it.
            list = _incomingMessages.get(sid);
        }
        list.push(message);
        _incomingMessages.put(sid,list);
    }

    public static Message popMessageQueue(long sid) {
        if (!_incomingMessages.containsKey(sid) || _incomingMessages.get(sid).isEmpty())
            return null;
        return _incomingMessages.get(sid).pop();
    }

    public static LinkedList<Message> getMessageQueue(long sid)
    {
        return _incomingMessages.getOrDefault(sid, null);
    }

    public static User getCurrentUser() {
        return _currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        _currentUser = currentUser;
    }

    public static void connect(String host, int port) throws IOException {
        _client = new ClientController(host, port);
    }

    public static ClientController getClient() {
        return _client;
    }

    public static void disconnect()
    {
        _client = null;
    }


    public static void setStatus(String status, Color color)
    {
        _lblStatus.setText(status);
        _lblStatus.setTextFill(color);
    }

    public static AnchorPane getPageRoot() {
        return _pageRoot;
    }
}
