package client.GUI.Helpers;

import javafx.scene.Node;

/**
 * An empty interface to identify gui controllers.
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
