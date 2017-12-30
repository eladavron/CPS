package client.GUI;

import client.ClientController;
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

/**
 * The main GUI application
 * @author Elad Avron
 */
public class CPSClientGUI extends Application{

    private volatile static CPSClientGUI _instance;
    private ClientController _client;

    private AnchorPane _pageRoot;
    private Label _lblStatus;

    public CPSClientGUI()
    {
        _instance = this;
    }

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
        changeGUI("Connection.fxml");

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
    public void changeGUI(String filename) throws IOException {
        URL guiURL = CPSClientGUI.class.getResource(filename);
        Node guiRoot = FXMLLoader.load(guiURL);
        _pageRoot.getChildren().removeAll();
        _pageRoot.getChildren().add(guiRoot);
        _pageRoot.setTopAnchor(guiRoot,0.0);
        _pageRoot.setBottomAnchor(guiRoot,0.0);
        _pageRoot.setLeftAnchor(guiRoot,0.0);
        _pageRoot.setRightAnchor(guiRoot, 0.0);
    }

    /**
     * Returns the instance of the app.
     * Useful for calling the changeGUI method.
     * @return Instance of CPSClientGUI
     */
    public static CPSClientGUI getInstance()
    {
        if (_instance == null){
            synchronized (CPSClientGUI.class) {
                if (_instance == null) {
                    _instance = new CPSClientGUI();
                }
            }
        }
        return _instance;
    }

    public void connect(String host, int port) throws IOException {
        try {
            _client = new ClientController(host, port);
            _lblStatus.setText("Connected to " + host + " on port " + port);
        } catch (IOException io)
        {
            _lblStatus.setText("Connection failed!");
            throw io;
        }
    }

    public void sendToServer(String message)
    {
        try {
            _client.sendToServer(message);
        } catch (IOException io)
        {
            _lblStatus.setText("Communication error!");
            _lblStatus.setTextFill(Color.RED);
        }
    }
}
