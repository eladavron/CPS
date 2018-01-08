package client;

import client.GUI.CPSClientGUI;
import com.fasterxml.jackson.core.JsonProcessingException;
import entity.Message;

import java.io.IOException;

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
        String json = (String) msg;
        Message receivedMessage = new Message(json);
        CPSClientGUI.addMessageToQueue(receivedMessage);
    }

    /**
     * Packages a message object nicely and sends it to the server.
     * @param message The message object to send
     * @throws JsonProcessingException When the message isn't converted ok.
     */
    public void sendMessageToServer(Message message) throws IOException {
        sendToServer(message.toJson());
    }
}