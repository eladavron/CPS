package client.GUI.Helpers;

import entity.Message;

/**
 * Implements a runner with Message and Throwable as optional parameters.
 * Used for processing incoming messages.
 */
public abstract class MessageRunnable implements Runnable {
    private Message _message;
    private Throwable _exception;

    /**
     * Set the <code>Message</code> object associated with this runner.
     * @param message An incoming Message object.
     */
    public void setMessage(Message message)
    {
        _message = message;
    }

    /**
     * Returns the message object associated with this runner.
     * @return A message object and such.
     */
    public Message getMessage() {
        return _message;
    }

    /**
     * Returns a string compiled of any errors supplied by the server and any exceptions associated with the runnable.
     * @return the compiled error string.
     */
    public String getErrorString()
    {
        String returnString = "";
        if (getMessage() != null
                && getMessage().getType() == Message.MessageType.FAILED
                && getMessage().getData() != null
                && getMessage().getData().get(0) != null)
        {
            returnString += (String)getMessage().getData().get(0) +"\n";
        }
        if (getException() != null)
            returnString += getException().getMessage();
        return returnString;
    }

    /**
     * Return any exception associated with this runnable.
     * @return
     */
    public Throwable getException() {
        return _exception;
    }

    /**
     * Sets the exception associated with this runnable.
     * @param exception
     */
    public void setException(Throwable exception) {
        this._exception = exception;
    }
}
