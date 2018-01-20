package client.GUI.Helpers;

/**
 * A "Refreshable" GUI Controller interface.<br>
 * Any element that extends this is declaring that it has refreshable elements, and can be called to refresh them on command.
 */
public interface Refreshable {

    /**
     * Refresh any refreshable elements within this GUI Controller.
     */
    public void refresh();
}
