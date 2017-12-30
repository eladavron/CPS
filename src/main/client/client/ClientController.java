package client;

import java.io.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the cps.client.
 *
 * @author Elad Avron
 */
public class ClientController extends ocsf.client.AbstractClient

{
    /**
     * Constructs an instance of the chat cps.client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     */

    public ClientController(String host, int port)
            throws IOException
    {
        super(host, port); //Call the superclass constructor
        openConnection();
    }

    /**
     * This method handles all data that comes in from the cps.server.
     *
     * @param msg The message from the cps.server.
     */
    public void handleMessageFromServer(Object msg)
    {
        //TODO: Parse Server Messages
    }

    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(String message) throws IOException {
        sendToServer(message);
    }
}