package client.GUI.Helpers;

import javafx.scene.Node;

/**
 * An empty interface to identify GUI controllers.<br>
 * Used for saving a history of shown GUI Nodes and associating them with their controllers, since JavaFX isn't smart
 * enough to it itself.<br>
 * Then again nothing in Java ever is.
 */
public class GUIController {
    private Node _node;
    public void setNode(Node node)
    {
        _node = node;
    }
    public Node getNode()
    {
        return _node;
    }
}
