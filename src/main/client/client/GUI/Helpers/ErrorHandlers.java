package client.GUI.Helpers;

import client.GUI.CPSClientGUI;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Custom error handlers.
 */
public class ErrorHandlers {

    /**
     * Shows a native error message when something goes wrong rendering or loading GUI elements.
     * This is a native java "popup" (Alert) so it shouldn't rely on GUI that much and should be used in cases where
     * custom GUI elements can't be trusted.
     * @param ex The original thrown exception. IO because GUI errors are all IO.
     * @param exit Whether or not to completely shut down the program after the error. Usually recommended.
     */
    public static void GUIError(IOException ex, boolean exit)
    {
        Alert guiError = new Alert(Alert.AlertType.ERROR);
        guiError.setHeaderText("An error occurred displaying the GUI!");
        guiError.setContentText("An error occurred displaying the GUI!");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        Label label = new Label("The exception stacktrace was:");
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        guiError.getDialogPane().setExpandableContent(expContent);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiError.showAndWait();
            }
        });
        if (CPSClientGUI.IS_DEBUG)
        {
            ex.printStackTrace();
        }
        if (exit)
        {
            System.exit(-1);
        }
    }
}
