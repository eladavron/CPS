package client.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class CPSClientGUI extends Application {

    private AnchorPane pageRoot;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL rootURL = getClass().getResource("GUIRoot.fxml");
        AnchorPane rootPane = FXMLLoader.load(rootURL);
        Scene scene = new Scene(rootPane);

        pageRoot = (AnchorPane) scene.lookup("#pageRoot");

        Helpers.LoadGUI(pageRoot, "LoginScreen.fxml");

        primaryStage.setScene(scene);
        primaryStage.setMinHeight(rootPane.getPrefHeight());
        primaryStage.setMinWidth(rootPane.getPrefWidth());
        primaryStage.show();
    }
}
