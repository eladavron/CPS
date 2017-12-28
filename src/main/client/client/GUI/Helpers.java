package client.GUI;

import javafx.fxml.*;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;


import java.io.IOException;
import java.net.URL;

public class Helpers {
    public static boolean LoadGUI(AnchorPane element, String filename)
    {
        try {
            URL guiURL = CPSClientGUI.class.getResource(filename);
            Node guiRoot = FXMLLoader.load(guiURL);
            element.getChildren().removeAll();
            element.getChildren().add(guiRoot);
            element.setTopAnchor(guiRoot,0.0);
            element.setBottomAnchor(guiRoot,0.0);
            element.setLeftAnchor(guiRoot,0.0);
            element.setRightAnchor(guiRoot, 0.0);
            return true;
        } catch (IOException e) {
            //TODO: Handle exception
            e.printStackTrace();
            return false;
        }
    }

    public static void ShowTooltip(Node parent, String message)
    {
        Tooltip tooltip = new Tooltip(message);
        tooltip.setAutoHide(true);
        Bounds boundInScene = parent.localToScreen(parent.getBoundsInLocal());
        tooltip.show(parent.getScene().getWindow(), boundInScene.getMaxX() + 5, boundInScene.getMinY());
    }
}
