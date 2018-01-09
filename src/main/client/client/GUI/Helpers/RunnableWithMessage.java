package client.GUI.Helpers;

import entity.Message;

/**
 * Implements Runnable but with a Message parameter.
 */
public abstract class RunnableWithMessage implements Runnable {
    private Message incoming;
    public void setMessage(Message message)
    {
        incoming = message;
    }

    public Message getIncoming() {
        return incoming;
    }
}
