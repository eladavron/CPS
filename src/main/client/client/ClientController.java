package client;

import Exceptions.InvalidMessageException;
import client.GUI.CPSClientGUI;
import entity.Message;

import java.io.IOException;

/**
 * Implements the OCSF Client Controller with some custom changes.
 */
public class ClientController extends ocsf.client.AbstractClient

{
    /**
     * Constructs an instance of the chat client and opens a connection.<br>
     * @param host The server to connect to.
     * @param port The port number to connect on.
     * @throws IOException in case a connection error occurred.
     */
    public ClientController(String host, int port)
            throws IOException
    {
        super(host, port); //Call the superclass constructor
        openConnection();
    }

    /**
     * This method handles all data that comes in from the server.<br>
     * It converts the string to a new Message objects and sends it for queueing.
     * @param msg The message from the server.
     */
    public void handleMessageFromServer(Object msg)
    {
        if (CPSClientGUI.IS_DEBUG)
        {
            System.out.println("RECEIVED: " + (String)msg);
        }
        String json = (String) msg;
        Message receivedMessage;
        try {
            receivedMessage = new Message(json);
        } catch (InvalidMessageException im) {
            receivedMessage = new Message(Message.MessageType.FAILED,
                    Message.DataType.PRIMITIVE,
                    "The server sent an unintelligible response.\nPlease contact system administrator for more information.");
            Long SID = Message.getSidFromJson(json);
            if (SID != null)
                receivedMessage.setTransID(SID);
        }
        CPSClientGUI.addMessageToQueue(receivedMessage);
    }
}