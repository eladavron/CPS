package client.GUI.Helpers;

import javafx.scene.control.Alert;

import java.io.IOException;

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
