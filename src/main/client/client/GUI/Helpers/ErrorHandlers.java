package client.GUI.Helpers;

import javafx.scene.control.Alert;

import java.io.IOException;

public class ErrorHandlers {
    public static void GUIError(IOException ex, boolean exit)
    {
        Alert guiError = new Alert(Alert.AlertType.ERROR);
        guiError.setHeaderText("An error occurred displaying the GUI!");
        guiError.setContentText("The following IO error occurred while trying to display the interface:\n"
                + ex.getMessage());
        guiError.showAndWait();
        ex.printStackTrace();
        if (exit)
        {
            System.exit(-1);
        }
    }
}
